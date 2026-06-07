import { Jimp } from 'jimp';
import { GifUtil, GifFrame, BitmapImage } from 'gifwrap';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, '..');
const logoPath = path.join(root, 'src/main/resources/static/images/loading-logo.png');
const outPath = path.join(root, 'src/main/resources/static/images/fleet-loading.gif');

const logo = await Jimp.read(logoPath);

const size = 280;
const frames = [];

for (let i = 0; i < 24; i += 1) {
    const canvas = new Jimp({ width: size, height: size, color: 0x00000000 });
    const phase = (i / 24) * Math.PI * 2;
    const scale = 0.9 + Math.sin(phase) * 0.08;

    const logoImg = logo.clone().resize({
        w: Math.max(1, Math.round(logo.width * scale * 0.78)),
        h: Math.max(1, Math.round(logo.height * scale * 0.78))
    });

    canvas.composite(logoImg, Math.round((size - logoImg.width) / 2), Math.round((size - logoImg.height) / 2));

    const bitmap = canvas.bitmap;
    frames.push(new GifFrame(new BitmapImage(bitmap), { delayCentisecs: 7 }));
}

GifUtil.quantizeDekker(frames, 256);
await GifUtil.write(outPath, frames);
console.log('Wrote', outPath);
