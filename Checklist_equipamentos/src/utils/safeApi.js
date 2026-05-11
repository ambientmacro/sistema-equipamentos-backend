// src/utils/safeApi.js

export async function safeRequest(fn, fallback = []) {
    try {
        return await fn();
    } catch (e) {
        console.error("Erro na API:", e);
        return fallback;
    }
}