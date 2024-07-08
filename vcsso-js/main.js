import BbsSign from "./bbs/bbsSign.js";
import {
    generateRandomStrings,
    convertObjectToArrayWithKeys,
    measureExecutionTime
} from "./utils.js"

(async () => {
    const numStrings = 20;     // Number of random strings
    const stringLength = 30;  // Length of each random string
    const randomStringsObject = generateRandomStrings(numStrings, stringLength);
    //console.log(randomStringsObject);

    const messages = convertObjectToArrayWithKeys(randomStringsObject);
    const bbs = new BbsSign();
    const msg_scalars = await bbs.prepare(messages);

    const bbsKeyPair = await bbs.generateKeyPair()
    const bbsSignature = await bbs.sign(msg_scalars, bbsKeyPair.privateKey, bbsKeyPair.publicKey);
    //console.log("BBS Sign", bbsSignature)

    const verified = await bbs.verify(msg_scalars, bbsSignature, bbsKeyPair.publicKey);
    console.log("Signature verified: ", verified)


    const disclosed_indexes = [0, 1,2];
    const disclosedMsgs = msg_scalars.filter(
        (m, i) => disclosed_indexes.includes(i)
    );

    //const proof = await bbs.generateProof(msg_scalars, bbsSignature, bbsKeyPair.publicKey, disclosed_indexes);
    const { result: genProofResult, timeTaken: genProofTIme } = await measureExecutionTime(
        bbs.generateProof.bind(bbs), msg_scalars, bbsSignature, bbsKeyPair.publicKey, disclosed_indexes
    );
    console.log(`{generateProof} Result:`, genProofResult);
    console.log(`Time taken: ${genProofTIme} ms`);

    const { result: proofVerifyResult, timeTaken: proofVerifyTime } = await measureExecutionTime(
        bbs.verifyProof.bind(bbs), genProofResult, bbsKeyPair.publicKey, disclosedMsgs, disclosed_indexes
    );
    console.log(`{verifyProof} Result:`, proofVerifyResult);
    console.log(`Time taken: ${proofVerifyTime} ms`);
})()