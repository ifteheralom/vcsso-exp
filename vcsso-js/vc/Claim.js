export default class Claim {
    #id;
    #name;
    #value;
    #signature;
    #valid;

    constructor(name, value) {
        this.#name = name;
        this.#value = value;
    }

    toString() {
        return `${this.#name}: ${this.#value}`
    }

    set id(value) {
        this.#id = value;
    }

    set signature(value) {
        this.#signature = value;
    }

    set valid(value) {
        this.#valid = value;
    }

    get id() {
        return this.#id;
    }

    get signature() {
        return this.#signature;
    }

    get valid() {
        return this.#valid;
    }

    get name() {
        return this.#name;
    }

    get value() {
        return this.#value;
    }
}