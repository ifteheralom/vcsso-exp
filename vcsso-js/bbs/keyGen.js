import {
    API_ID_BBS_SHAKE,
    keyGen,
    publicFromPrivate
} from '@grottonetworking/bbs-signatures';
import crypto from 'crypto';
import KeyPair from "./KeyPair.js";

export default class KeyGen {
    #bytesLength;


    constructor(bytesLength) {
        this.#bytesLength = bytesLength;
    }

    async generateKeyPair() {
        const keyMaterial = new Uint8Array(crypto.randomBytes(bytesLength).buffer);
        const keyInfo = new TextEncoder().encode('BBS-Example Key info');

        const sk_bytes = await keyGen(keyMaterial, keyInfo, API_ID_BBS_SHAKE);
        const pub_bytes = publicFromPrivate(sk_bytes);

        return new KeyPair(pub_bytes, sk_bytes);
    }
}