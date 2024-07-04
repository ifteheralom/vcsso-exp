import {
    API_ID_BBS_SHAKE,
    bytesToHex,
    keyGen,
    publicFromPrivate} from '@grottonetworking/bbs-signatures';
import crypto from 'crypto';
import KeyPair from "../bbs/KeyPair.js";

export default class SigningService {
    constructor() {
    }

    async generateKeyPair() {
        const bytesLength = 40; // >= 32 bytes
        const keyMaterial = new Uint8Array(crypto.randomBytes(bytesLength).buffer);
        const keyInfo = new TextEncoder().encode('BBS-Example Key info');
        const sk_bytes = await keyGen(keyMaterial, keyInfo, API_ID_BBS_SHAKE);

        console.log(`Private key, length ${sk_bytes.length}, (hex):`);
        console.log(bytesToHex(sk_bytes));
        const pub_bytes = publicFromPrivate(sk_bytes);
        console.log(`Public key, length ${pub_bytes.length}, (hex):`);
        console.log(bytesToHex(pub_bytes));

        return new KeyPair(pub_bytes, sk_bytes)
    }
}