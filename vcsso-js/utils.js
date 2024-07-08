export function generateRandomStrings(numStrings, stringLength) {
    const result = {};
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

    for (let i = 0; i < numStrings; i++) {
        let randomString = '';
        for (let j = 0; j < stringLength; j++) {
            const randomIndex = Math.floor(Math.random() * characters.length);
            randomString += characters[randomIndex];
        }
        result[`string${i + 1}`] = randomString;
    }

    return result;
}

export function convertObjectToArrayWithKeys(obj) {
    return Object.entries(obj).map(([key, value]) => `${key}:${value}`);
}

export async function measureExecutionTime(fn, ...args) {
    const start = performance.now();
    const result = await fn(...args);
    const end = performance.now();
    const timeTaken = end - start;

    return {
        result,
        timeTaken
    };
}