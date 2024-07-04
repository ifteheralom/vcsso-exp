import Metadata from './Metadata.js'
import Subject from  './Subject.js';
import Claim from './Claim.js';

export default class Proof {
    #type;
    #created;
    #verifyMethod;
    #jwt
    #signature;

    constructor(type, created, verifyMethod, jwt, signature) {
        this.#type = type;
        this.#created = created;
        this.#verifyMethod = verifyMethod;
        this.#jwt = jwt;
        this.#signature = signature;
    }

    toString() {
        return JSON.stringify({
            "type": this.type,
            "created": this.created,
            "verifyMethod": this.verifyMethod,
            "jwt": this.jwt,
            "signature": this.signature,

        });
    }

    get type() {
        return this.#type;
    }

    get created() {
        return this.#created;
    }

    get verifyMethod() {
        return this.#verifyMethod;
    }

    get jwt() {
        return this.#jwt;
    }

    get signature() {
        return this.#signature;
    }
}