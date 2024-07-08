import Metadata from './vc/Metadata.js'
import Subject from  './vc/Subject.js';
import Claim from './vc/Claim.js';
import Proof from './vc/Proof.js';
import Credential from './vc/Credential.js';
import JWTManager from "./jwt/JWTManager.js";
import BbsSign from "./bbs/bbsSign.js";

let credentialSubject = new Subject(123456789);
credentialSubject.addClaim(new Claim("name", "Harry James Potter"));
credentialSubject.addClaim(new Claim("job", "Dark Arts Research Fellow"));
credentialSubject.addClaim(new Claim("university", "Hogwarts School of Witchcraft and Wizardry"));
credentialSubject.addClaim(new Claim("id", "0123456789"));
credentialSubject.addClaim(new Claim("email", "harry@hogwarts.magics"));

let vcTime = new Date();
let vcExTime = new Date();
vcExTime.setDate(vcExTime.getDate() + 30)

let credentialMetadata = new Metadata(
    "vc12345",
    "HogwartsCredential",
    "Hogwarts.magics",
    vcTime,
    vcExTime,
    true
);

let credentialProof = new Proof(
    "",
    "",
    "",
    "",
    ""
);

let verifiableCredential = new Credential(
    credentialMetadata,
    credentialSubject,
    credentialProof
);

console.log(
    JSON.parse(
        verifiableCredential.toString()
    )
);

let jwtManager = new JWTManager();
const { publicKey, privateKey } = await jwtManager.generateKeyPair();
let token = await jwtManager.generateJWT(JSON.parse(verifiableCredential.toString()), privateKey)
//console.log(token)

const messages = credentialSubject.toArray;
const bbs = new BbsSign();
const msg_scalars = await bbs.prepare(messages);

const bbsKeyPair = await bbs.generateKeyPair()

const bbsSignature = await bbs.sign(msg_scalars, bbsKeyPair.privateKey, bbsKeyPair.publicKey);
console.log("BBS Sign", bbsSignature)

const verified = await bbs.verify(msg_scalars, bbsSignature, bbsKeyPair.publicKey);
console.log(verified)


const disclosed_indexes = [0, 1,2];
const disclosedMsgs = msg_scalars.filter(
    (m, i) => disclosed_indexes.includes(i)
);

const proof = await bbs.generateProof(msg_scalars, bbsSignature, bbsKeyPair.publicKey, disclosed_indexes);
console.log("BBS Proof", proof)
console.log("BBS Proof", proof.length)

const proofValid = await bbs.verifyProof(proof, bbsKeyPair.publicKey, disclosedMsgs, disclosed_indexes);
console.log(proofValid)