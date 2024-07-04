export default class Subject {
    #id;
    #claims = {};

    constructor(id) {
        this.#id = id;
    }

    toString() {
        return JSON.stringify({
            "id": this.#id,
            "claims": this.#claims
        });
    }

    addClaim(claim) {
        this.#claims[claim.name] = claim.value
    }

    get id() {
        return this.#id;
    }

    get claims() {
        return this.#claims;
    }

    get size() {
        return Object.keys(this.#claims).length;
    }

    get toArray() {
        let res = [];
        for(const k in this.claims) {
            res.push(`${k}:${this.#claims[k]}`);
        }
        return res;
    }
}