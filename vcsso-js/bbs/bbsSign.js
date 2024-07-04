import {
    API_ID_BBS_SHAKE,
    bytesToHex, hexToBytes, messages_to_scalars,
    prepareGenerators, publicFromPrivate,
    proofGen, proofVerify,
    sign, verify, keyGen
} from '@grottonetworking/bbs-signatures';
import crypto from "crypto";
import KeyPair from "./KeyPair.js";

export default class BbsSign {
    #header
    #gens;
    #ph

    constructor() {
    }

    async prepare(messages) {
        this.#gens = await prepareGenerators(messages.length, API_ID_BBS_SHAKE);
        this.#header = hexToBytes("");
        this.#ph = new Uint8Array();

        const textEncoder = new TextEncoder();
        const messagesOctets = messages.map(msg => textEncoder.encode(msg));
        const msg_scalars = await messages_to_scalars(messagesOctets, API_ID_BBS_SHAKE);

        return msg_scalars;
    }

    async generateKeyPair(bytesLength) {
        const keyMaterial = new Uint8Array(crypto.randomBytes(128).buffer);
        const keyInfo = new TextEncoder().encode('BBS-Example Key info');

        const sk_bytes = await keyGen(keyMaterial, keyInfo, API_ID_BBS_SHAKE);
        const pub_bytes = publicFromPrivate(sk_bytes);

        return new KeyPair(pub_bytes, sk_bytes);
    }

    async sign(msg_scalars, sk_bytes, pk_bytes) {
        const signature = await sign(
            sk_bytes,
            pk_bytes,
            this.#header,
            msg_scalars,
            this.#gens,
            API_ID_BBS_SHAKE
        );

        return bytesToHex(signature);
    }

    async verify (msg_scalars, signature, pk_bytes) {
        const verified = await verify(
            pk_bytes,
            hexToBytes(signature),
            this.#header,
            msg_scalars,
            this.#gens,
            API_ID_BBS_SHAKE
        );

        return verified;
    }

    async generateProof(msg_scalars, signature, pk_bytes, disclosed_indexes) {
        const proof = await proofGen(
            pk_bytes,
            hexToBytes(signature),
            this.#header,
            this.#ph,
            msg_scalars,
            disclosed_indexes,
            this.#gens,
            API_ID_BBS_SHAKE
        );

        return bytesToHex(proof);
    }

    async verifyProof(proof, pk_bytes, disclosedMsgs, disclosed_indexes) {
        const proofValid = await proofVerify(
            pk_bytes,
            hexToBytes(proof),
            this.#header,
            this.#ph,
            disclosedMsgs,
            disclosed_indexes,
            this.#gens,
            API_ID_BBS_SHAKE);

        return proofValid;
    }
}