import Metadata from './Metadata.js'
import Subject from  './Subject.js';
import Proof from './Proof.js'

export default class Credential {
    #metadata;
    #subject;
    #proof

    constructor(metadata, subject, proof) {
        this.#metadata = metadata;
        this.#subject = subject;
        this.#proof = proof;
    }

    toString() {
        return JSON.stringify({
            "metadata": this.#metadata.toString(),
            "subject": this.#subject.toString(),
            "proofs": this.#proof.toString()
        });
    }

    get metadata() {
        return this.#metadata;
    }

    get subject() {
        return this.#subject;
    }

    get proof() {
        return this.#proof;
    }
}
