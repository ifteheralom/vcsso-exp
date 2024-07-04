import {
    bytesToHex,
} from "@grottonetworking/bbs-signatures";

export default class KeyPair {
    #publicKey
    #privateKey

    constructor(publicKey, privateKey) {
        this.#publicKey = publicKey;
        this.#privateKey = privateKey;
    }

    getHexValue(key_bytes) {
        return bytesToHex(key_bytes);
    }

    getLength(key_bytes) {
        return key_bytes.length;
    }

    get publicKey() {
        return this.#publicKey;
    }

    get privateKey() {
        return this.#privateKey;
    }
}