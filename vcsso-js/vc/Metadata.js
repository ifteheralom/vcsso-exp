export default class Metadata {
    #id;
    #type;
    #issuer;
    #issueDate;
    #expireDate;
    #validity;

    constructor(id, type, issuer, issueDate, expireDate, validity) {
        this.#id = id;
        this.#type = type;
        this.#issuer = issuer;
        this.#issueDate = issueDate;
        this.#expireDate = expireDate;
        this.#validity = validity;
    }

    toString() {
        return JSON.stringify({
            "id": this.#id,
            "type": this.#type,
            "issuer": this.#issuer,
            "issueDate": this.#issueDate,
            "expireDate": this.#expireDate,
            "validity": this.#validity,
        });
    }

    get id() {
        return this.#id;
    }

    get type() {
        return this.#type;
    }

    get issuer() {
        return this.#issuer;
    }

    get issueDate() {
        return this.#issueDate;
    }

    get expireDate() {
        return this.#expireDate;
    }

    get validity() {
        return this.#validity;
    }
}