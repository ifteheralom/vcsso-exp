use std::collections::{BTreeMap, BTreeSet};
use std::time::{Duration, Instant};
use bbs_plus::prelude::{Signature23G1};
use blake2::Blake2b512;
use bbs_plus::setup::{KeypairG2, SignatureParams23G1};
use rand::prelude::*;
use ark_ec::{pairing::Pairing};
use ark_bls12_381::{Bls12_381, Fr};
use ark_std::{rand::{rngs::StdRng, SeedableRng}, UniformRand};
use bbs_plus::proof_23::{PoKOfSignature23G1Protocol};
use dock_crypto_utils::{signature::{MessageOrBlinding}};
use schnorr_pok::compute_random_oracle_challenge;

pub fn print_hello() {
    println!("Hello World!");
}

// pub fn setup_keys() {
//     let array = [0u8, 0u8, 0u8, 1u8, 0u8, 0u8, 0u8, 1u8];
//     let seed : &[u8] = &array;
//     let mut rng = rand::thread_rng();
//
//     let params = SignatureParams23G1::<Bls12_381>::generate_using_rng(&mut rng, 5);
//     let params_1 = SignatureParams23G1::<Bls12_381>::new::<Blake2b512>(&[1, 2, 3, 4], 5);
//
//     let keypair = KeypairG2::<Bls12_381>::generate_using_rng_and_bbs23_params(&mut rng, &params);
//     let keypair_1 = KeypairG2::<Bls12_381>::generate_using_seed_and_bbs23_params::<Blake2b512>(seed, &params);
//
//     let sk = SecretKey::generate_using_seed::<Blake2b512>(&seed);
//     let pk = PublicKeyG2::generate_using_secret_key_and_bbs23_params(&sk, &params);
//     // KeypairG2 {secret_key: sk, public_key: pk}
//
//     println!("{:?}", sk);
// }
//
// pub fn bbs_sign() {
//     type Fr = <Bls12_381 as Pairing>::ScalarField;
//     let mut rng = StdRng::seed_from_u64(0u64);
//     let message_count = 20;
//     let messages: Vec<Fr> = (0..message_count).map(|_| Fr::rand(&mut rng)).collect();
//
//     let params = SignatureParams23G1::<Bls12_381>::generate_using_rng(&mut rng, message_count);
//     let keypair =
//         KeypairG2::<Bls12_381>::generate_using_rng_and_bbs23_params(&mut rng, &params);
//
//     let start = Instant::now();
//     // All messages are known to signer
//     let sig =
//         Signature23G1::<Bls12_381>::new(&mut rng, &messages, &keypair.secret_key, &params)
//             .unwrap();
//     println!(
//         "Time to sign multi-message of size {} is {:?}",
//         message_count,
//         start.elapsed()
//     );
//
//     let (verif_params, verif_pk) = (
//         PreparedSignatureParams23G1::from(params.clone()),
//         PreparedPublicKeyG2::from(keypair.public_key.clone()),
//     );
//
//     let mut zero_sig = sig.clone();
//     zero_sig.A = G1Affine::zero();
//     assert!(zero_sig
//         .verify(&messages, verif_pk.clone(), verif_params.clone())
//         .is_err());
//
//     let start = Instant::now();
//     sig.verify(&messages, verif_pk, verif_params).unwrap();
//     println!(
//         "Time to verify signature over multi-message of size {} is {:?}",
//         message_count,
//         start.elapsed()
//     );
//
//     drop(sig);
// }

pub fn sig_setup<R: RngCore>(
    rng: &mut R,
    message_count: u32,
) -> (
    Vec<Fr>,
    SignatureParams23G1<Bls12_381>,
    KeypairG2<Bls12_381>,
    Signature23G1<Bls12_381>,
) {
    let messages: Vec<Fr> = (0..message_count).map(|_| Fr::rand(rng)).collect();
    let params = SignatureParams23G1::<Bls12_381>::generate_using_rng(rng, message_count);
    let keypair = KeypairG2::<Bls12_381>::generate_using_rng_and_bbs23_params(rng, &params);
    let sig =
        Signature23G1::<Bls12_381>::new(rng, &messages, &keypair.secret_key, &params).unwrap();
    (messages, params, keypair, sig)
}


pub fn pok_signature_revealed_message() {
    // Create and verify proof of knowledge of a signature when some messages are revealed
    let mut rng = StdRng::seed_from_u64(0u64);
    let message_count = 20;
    let (messages, params, keypair, sig) = sig_setup(&mut rng, message_count);
    sig.verify(&messages, keypair.public_key.clone(), params.clone())
        .unwrap();

    let mut revealed_indices = BTreeSet::new();
    revealed_indices.insert(0);
    revealed_indices.insert(2);

    let mut revealed_msgs = BTreeMap::new();
    for i in revealed_indices.iter() {
        revealed_msgs.insert(*i, messages[*i]);
    }

    let mut proof_create_duration = Duration::default();
    let start = Instant::now();
    let pok = PoKOfSignature23G1Protocol::init(
        &mut rng,
        None,
        None,
        &sig,
        &params,
        messages.iter().enumerate().map(|(idx, msg)| {
            if revealed_indices.contains(&idx) {
                MessageOrBlinding::RevealMessage(msg)
            } else {
                MessageOrBlinding::BlindMessageRandomly(msg)
            }
        }),
    )
        .unwrap();
    proof_create_duration += start.elapsed();

    // Protocol can be serialized
    //test_serialization!(PoKOfSignature23G1Protocol<Bls12_381>, pok);

    let mut chal_bytes_prover = vec![];
    pok.challenge_contribution(&revealed_msgs, &params, &mut chal_bytes_prover)
        .unwrap();
    let challenge_prover =
        compute_random_oracle_challenge::<Fr, Blake2b512>(&chal_bytes_prover);

    let start = Instant::now();
    let proof = pok.gen_proof(&challenge_prover).unwrap();
    proof_create_duration += start.elapsed();

    let public_key = &keypair.public_key;
    assert!(params.is_valid());
    assert!(public_key.is_valid());

    let mut chal_bytes_verifier = vec![];
    proof
        .challenge_contribution(&revealed_msgs, &params, &mut chal_bytes_verifier)
        .unwrap();
    let challenge_verifier =
        compute_random_oracle_challenge::<Fr, Blake2b512>(&chal_bytes_verifier);

    assert_eq!(chal_bytes_prover, chal_bytes_verifier);

    let mut proof_verif_duration = Duration::default();
    let start = Instant::now();
    proof
        .verify(
            &revealed_msgs,
            &challenge_verifier,
            public_key.clone(),
            params.clone(),
        )
        .unwrap();
    proof_verif_duration += start.elapsed();

    // Proof can be serialized
    //test_serialization!(PoKOfSignature23G1Proof<Bls12_381>, proof);

    println!(
        "Time to create proof with message size {} and revealing {} messages is {:?}",
        message_count,
        revealed_indices.len(),
        proof_create_duration
    );
    println!(
        "Time to verify proof with message size {} and revealing {} messages is {:?}",
        message_count,
        revealed_indices.len(),
        proof_verif_duration
    );
}