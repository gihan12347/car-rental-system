/**
 * Generates favicon + PWA icons from the rental car logo.
 * Run: node scripts/generate-favicons.mjs
 */
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import sharp from 'sharp';
import pngToIco from 'png-to-ico';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, '..');

const SOURCE = path.join(__dirname, 'logo-source.png');

const OUT = path.join(root, 'src', 'main', 'resources', 'static', 'icons');
const TRANSPARENT = { r: 0, g: 0, b: 0, alpha: 0 };

async function renderSquareIcon(input, size, paddingRatio = 0.12) {
    const trimmed = await input.clone().trim();
    const inner = Math.round(size * (1 - paddingRatio * 2));
    const pad = Math.max(0, Math.floor((size - inner) / 2));

    return trimmed
        .resize(inner, inner, { fit: 'contain', background: TRANSPARENT })
        .extend({
            top: pad,
            bottom: size - inner - pad,
            left: pad,
            right: size - inner - pad,
            background: TRANSPARENT,
        })
        .png()
        .toBuffer();
}

async function main() {
    if (!fs.existsSync(SOURCE)) {
        console.error('Source image not found:', SOURCE);
        process.exit(1);
    }
    fs.mkdirSync(OUT, { recursive: true });

    const base = sharp(SOURCE).ensureAlpha();

    const sizes = [
        { name: 'favicon-16.png', size: 16, pad: 0.14 },
        { name: 'favicon-32.png', size: 32, pad: 0.12 },
        { name: 'favicon-48.png', size: 48, pad: 0.12 },
        { name: 'apple-touch-icon.png', size: 180, pad: 0.1 },
        { name: 'icon-192.png', size: 192, pad: 0.1 },
        { name: 'icon-512.png', size: 512, pad: 0.1 },
    ];

    const pngBuffers = {};
    for (const { name, size, pad } of sizes) {
        const outPath = path.join(OUT, name);
        const buf = await renderSquareIcon(base, size, pad);
        await fs.promises.writeFile(outPath, buf);
        pngBuffers[size] = buf;
        console.log('Wrote', name);
    }

    const ico = await pngToIco([pngBuffers[16], pngBuffers[32], pngBuffers[48]]);
    await fs.promises.writeFile(path.join(OUT, 'favicon.ico'), ico);
    console.log('Wrote favicon.ico');
}

main().catch((err) => {
    console.error(err);
    process.exit(1);
});
