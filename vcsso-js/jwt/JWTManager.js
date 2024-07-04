import jwt from "jsonwebtoken"
import * as crypto from "crypto";

export default class JWTManager {

    constructor() {
    }

    async generateKeyPair (){
        // console.log(crypto.getCurves())
        return crypto.generateKeyPairSync("ec", {
            //modulusLength: 2048,
            namedCurve: 'prime256v1',
            publicKeyEncoding: { type: "spki", format: "pem" },
            privateKeyEncoding: { type: "pkcs8", format: "pem" },
        });
    }

    generateJWT(payload, privateKey) {
        return new Promise((resolve, reject) => {
            jwt.sign(
                payload,
                privateKey,
                { algorithm: 'ES256' },
                function(err, token) {
                    if (err != null) reject(err);
                    resolve(token);
                }
            );
        });
    }

}