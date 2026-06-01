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
  var url;
  try {
    url = new URL(event.request.url);
  } catch (e) {
    event.respondWith(fetch(event.request));
    return;
  }
  if (url.pathname.indexOf('/login') !== -1 || url.pathname.indexOf('/api/auth/status') !== -1) {
    event.respondWith(fetch(event.request, { cache: 'no-store' }));
    return;
  }
  event.respondWith(fetch(event.request));
});
