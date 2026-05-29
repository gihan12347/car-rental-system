/**
 * Service worker for FleetDesk PWA install (Chrome Android / desktop).
 * Must handle fetch with respondWith — empty handlers fail installability on newer Chrome.
 */
self.addEventListener('install', function (event) {
  self.skipWaiting();
});

self.addEventListener('activate', function (event) {
  event.waitUntil(self.clients.claim());
});

self.addEventListener('fetch', function (event) {
  if (event.request.method !== 'GET') {
    return;
  }
  event.respondWith(fetch(event.request));
});
