use std::collections::{BTreeMap, BTreeSet};
use std::time::{Duration, Instant};
use blake2::Blake2b512;
use bbs_plus::setup::{PublicKeyG2, SignatureParams23G1, SecretKey};
use rand::prelude::*;
use ark_ec::{pairing::Pairing};
use ark_bls12_381::{Bls12_381, Fr};
use ark_ff::PrimeField;
use ark_std::{rand::{rngs::StdRng, SeedableRng}, UniformRand, Zero};
use bbs_plus::threshold::multiplication_phase::Phase2;
use bbs_plus::threshold::randomness_generation_phase::Phase1;
use bbs_plus::threshold::threshold_bbs::BBSSignatureShare;
use oblivious_transfer_protocols::ot_based_multiplication::{
    dkls18_mul_2p::MultiplicationOTEParams, dkls19_batch_mul_2p::GadgetVector
};
use oblivious_transfer_protocols::{
    base_ot::simplest_ot::{OneOfTwoROTSenderKeys, ROTReceiverKeys},
    ot_based_multiplication::base_ot_multi_party_pairwise::{
        BaseOTOutput, Participant as BaseOTParty,
    },
    Bit, ParticipantId,
};
use secret_sharing_and_dkg::shamir_ss::deal_random_secret;

pub fn print_hello() {
    println!("Hello World!");
}

pub fn trusted_party_keygen<R: RngCore, F: PrimeField>(
    rng: &mut R,
    threshold: ParticipantId,
    total: ParticipantId,
) -> (F, Vec<F>) {
    let (secret, shares, _) = deal_random_secret(rng, threshold, total).unwrap();
    (secret, shares.0.into_iter().map(|s| s.share).collect())
}

pub fn check_base_ot_keys(
    choices: &[Bit],
    receiver_keys: &ROTReceiverKeys,
    sender_keys: &OneOfTwoROTSenderKeys,
) {
    for i in 0..sender_keys.len() {
        if choices[i] {
            assert_eq!(sender_keys.0[i].1, receiver_keys.0[i]);
        } else {
            assert_eq!(sender_keys.0[i].0, receiver_keys.0[i]);
        }
    }
}

pub fn do_pairwise_base_ot<const KEY_SIZE: u16>(
    rng: &mut StdRng,
    num_base_ot: u16,
    num_parties: u16,
    all_party_set: BTreeSet<ParticipantId>,
) -> Vec<BaseOTOutput> {
    #[allow(non_snake_case)]
        let B = <Bls12_381 as Pairing>::G1Affine::rand(rng);
    let mut base_ots = vec![];
    let mut sender_pks = BTreeMap::new();
    let mut receiver_pks = BTreeMap::new();

    for i in 1..=num_parties {
        let mut others = all_party_set.clone();
        others.remove(&i);
        let (base_ot, sender_pk_and_proof) =
            BaseOTParty::init::<_, Blake2b512>(rng, i, others, num_base_ot, &B).unwrap();
        base_ots.push(base_ot);
        sender_pks.insert(i, sender_pk_and_proof);
    }

    for (sender_id, pks) in sender_pks {
        for (id, pk) in pks {
            let recv_pk = base_ots[id as usize - 1]
                .receive_sender_pubkey::<_, Blake2b512, KEY_SIZE>(rng, sender_id, pk, &B)
                .unwrap();
            receiver_pks.insert((id, sender_id), recv_pk);
        }
    }

    let mut challenges = BTreeMap::new();
    let mut responses = BTreeMap::new();
    let mut hashed_keys = BTreeMap::new();

    for ((sender, receiver), pk) in receiver_pks {
        let chal = base_ots[receiver as usize - 1]
            .receive_receiver_pubkey::<KEY_SIZE>(sender, pk)
            .unwrap();
        challenges.insert((receiver, sender), chal);
    }

    for ((sender, receiver), chal) in challenges {
        let resp = base_ots[receiver as usize - 1]
            .receive_challenges(sender, chal)
            .unwrap();
        responses.insert((receiver, sender), resp);
    }

    for ((sender, receiver), resp) in responses {
        let hk = base_ots[receiver as usize - 1]
            .receive_responses(sender, resp)
            .unwrap();
        hashed_keys.insert((receiver, sender), hk);
    }

    for ((sender, receiver), hk) in hashed_keys {
        base_ots[receiver as usize - 1]
            .receive_hashed_keys(sender, hk)
            .unwrap()
    }

    let mut base_ot_outputs = vec![];
    for b in base_ots {
        base_ot_outputs.push(b.finish());
    }

    for base_ot in &base_ot_outputs {
        for (other, sender_keys) in &base_ot.sender_keys {
            let (choices, rec_keys) = base_ot_outputs[*other as usize - 1]
                .receiver
                .get(&base_ot.id)
                .unwrap();
            assert_eq!(rec_keys.len(), sender_keys.len());
            check_base_ot_keys(&choices, &rec_keys, &sender_keys);
        }
    }
    base_ot_outputs
}

pub fn signing() {
    let mut rng = StdRng::seed_from_u64(0u64);
    const BASE_OT_KEY_SIZE: u16 = 128;
    const KAPPA: u16 = 256;
    const STATISTICAL_SECURITY_PARAMETER: u16 = 80;
    let ote_params = MultiplicationOTEParams::<KAPPA, STATISTICAL_SECURITY_PARAMETER> {};
    let gadget_vector = GadgetVector::<Fr, KAPPA, STATISTICAL_SECURITY_PARAMETER>::new::<
        Blake2b512,
    >(ote_params, b"test-gadget-vector");

    let protocol_id = b"test".to_vec();

    let sig_batch_size = 1;
    let threshold_signers = 5;
    let total_signers = 8;
    let all_party_set = (1..=total_signers).into_iter().collect::<BTreeSet<_>>();
    let threshold_party_set = (1..=threshold_signers).into_iter().collect::<BTreeSet<_>>();

    // The signers do a keygen. This is a one time setup.
    let (sk, sk_shares) =
        trusted_party_keygen::<_, Fr>(&mut rng, threshold_signers, total_signers);

    // The signers run OT protocol instances. This is also a one time setup.
    let base_ot_outputs = do_pairwise_base_ot::<BASE_OT_KEY_SIZE>(
        &mut rng,
        ote_params.num_base_ot(),
        total_signers,
        all_party_set.clone(),
    );

    let message_count = 5;
    let params = SignatureParams23G1::<Bls12_381>::generate_using_rng(&mut rng, message_count);
    let public_key =
        PublicKeyG2::generate_using_secret_key_and_bbs23_params(&SecretKey(sk), &params);

    println!(
        "For a batch size of {} BBS signatures and {} signers",
        sig_batch_size, threshold_signers
    );

    // Following have to happen for each new batch of signatures. Batch size can be 1 when creating one signature at a time

    let mut round1s = vec![];
    let mut commitments = vec![];
    let mut commitments_zero_share = vec![];
    let mut round1outs = vec![];

    // Signers initiate round-1 and each signer sends commitments to others
    let start = Instant::now();
    for i in 1..=threshold_signers {
        let mut others = threshold_party_set.clone();
        others.remove(&i);
        let (round1, comm, comm_zero) = Phase1::<Fr, 256>::init_for_bbs(
            &mut rng,
            sig_batch_size,
            i,
            others,
            protocol_id.clone(),
        )
            .unwrap();
        round1s.push(round1);
        commitments.push(comm);
        commitments_zero_share.push(comm_zero);
    }

    // Signers process round-1 commitments received from others
    for i in 1..=threshold_signers {
        for j in 1..=threshold_signers {
            if i != j {
                round1s[i as usize - 1]
                    .receive_commitment(
                        j,
                        commitments[j as usize - 1].clone(),
                        commitments_zero_share[j as usize - 1]
                            .get(&i)
                            .unwrap()
                            .clone(),
                    )
                    .unwrap();
            }
        }
    }

    // Signers create round-1 shares once they have the required commitments from others
    for i in 1..=threshold_signers {
        for j in 1..=threshold_signers {
            if i != j {
                let share = round1s[j as usize - 1].get_comm_shares_and_salts();
                let zero_share = round1s[j as usize - 1]
                    .get_comm_shares_and_salts_for_zero_sharing_protocol_with_other(&i);
                round1s[i as usize - 1]
                    .receive_shares(j, share, zero_share)
                    .unwrap();
            }
        }
    }

    // Signers finish round-1 to generate the output
    let mut expected_sk = Fr::zero();
    for (i, round1) in round1s.into_iter().enumerate() {
        let out = round1.finish_for_bbs::<Blake2b512>(&sk_shares[i]).unwrap();
        expected_sk += out.masked_signing_key_shares.iter().sum::<Fr>();
        round1outs.push(out);
    }
    println!("Phase 1 took {:?}", start.elapsed());

    assert_eq!(expected_sk, sk * Fr::from(sig_batch_size));
    for i in 1..threshold_signers {
        assert_eq!(round1outs[0].e, round1outs[i as usize].e);
    }

    let mut round2s = vec![];
    let mut all_msg_1s = vec![];

    // Signers initiate round-2 and each signer sends messages to others
    let start = Instant::now();
    for i in 1..=threshold_signers {
        let mut others = threshold_party_set.clone();
        others.remove(&i);
        let (phase, U) = Phase2::init(
            &mut rng,
            i,
            round1outs[i as usize - 1].masked_signing_key_shares.clone(),
            round1outs[i as usize - 1].masked_rs.clone(),
            base_ot_outputs[i as usize - 1].clone(),
            others,
            ote_params,
            &gadget_vector,
        )
            .unwrap();
        round2s.push(phase);
        all_msg_1s.push((i, U));
    }

    // Signers process round-2 messages received from others
    let mut all_msg_2s = vec![];
    for (sender_id, msg_1s) in all_msg_1s {
        for (receiver_id, m) in msg_1s {
            let m2 = round2s[receiver_id as usize - 1]
                .receive_message1::<Blake2b512>(sender_id, m, &gadget_vector)
                .unwrap();
            all_msg_2s.push((receiver_id, sender_id, m2));
        }
    }

    for (sender_id, receiver_id, m2) in all_msg_2s {
        round2s[receiver_id as usize - 1]
            .receive_message2::<Blake2b512>(sender_id, m2, &gadget_vector)
            .unwrap();
    }

    let round2_outputs = round2s.into_iter().map(|p| p.finish()).collect::<Vec<_>>();
    println!("Phase 2 took {:?}", start.elapsed());

    // Check that multiplication phase ran successfully, i.e. each signer has an additive share of
    // a multiplication with every other signer
    for i in 1..=threshold_signers {
        for (j, z_A) in &round2_outputs[i as usize - 1].0.z_A {
            let z_B = round2_outputs[*j as usize - 1].0.z_B.get(&i).unwrap();
            for k in 0..sig_batch_size as usize {
                assert_eq!(
                    z_A.0[k] + z_B.0[k],
                    round1outs[i as usize - 1].masked_signing_key_shares[k]
                        * round1outs[*j as usize - 1].masked_rs[k]
                );
                assert_eq!(
                    z_A.1[k] + z_B.1[k],
                    round1outs[i as usize - 1].masked_rs[k]
                        * round1outs[*j as usize - 1].masked_signing_key_shares[k]
                );
            }
        }
    }

    // This is the final step where each signer generates his share of the signature without interaction
    // with any other signer and sends this share to the client
    let mut sig_shares_time = Duration::default();
    let mut sig_aggr_time = Duration::default();
    for k in 0..sig_batch_size as usize {
        let messages = (0..message_count)
            .into_iter()
            .map(|_| Fr::rand(&mut rng))
            .collect::<Vec<_>>();

        println!("{:?}", messages);

        // Get shares from a threshold number of signers
        let mut shares = vec![];
        let start = Instant::now();
        for i in 0..threshold_signers as usize {
            let share = BBSSignatureShare::new(
                &messages,
                k,
                &round1outs[i],
                &round2_outputs[i],
                &params,
            )
                .unwrap();
            shares.push(share);
        }
        sig_shares_time += start.elapsed();

        // Client aggregate the shares to get the final signature
        let start = Instant::now();
        let sig = BBSSignatureShare::aggregate(shares).unwrap();
        sig_aggr_time += start.elapsed();
        sig.verify(&messages, public_key.clone(), params.clone())
            .unwrap();
    }

    println!("Generating signature shares took {:?}", sig_shares_time);
    println!("Aggregating signature shares took {:?}", sig_aggr_time);
}