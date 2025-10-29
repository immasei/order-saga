(() => {
  const toInput = document.getElementById('to');
  const loadBtn = document.getElementById('load');
  const container = document.getElementById('messages');
  let es = null;
  const renderedIds = new Set();

  function render(list) {
    container.innerHTML = '';
    renderedIds.clear();
    list.forEach(addCard);
  }

  function addCard(m) {
    if (!m || !m.id) return;
    if (renderedIds.has(m.id)) return;
    const div = document.createElement('div');
    div.className = 'msg';
    div.innerHTML = `<div class="sub">${escapeHtml(m.subject)}</div>
                     <div>${escapeHtml(m.body)}</div>
                     <div class="meta">to: ${escapeHtml(m.toAddress)} | status: ${m.status} | id: ${m.id}</div>`;
    container.prepend(div);
    renderedIds.add(m.id);
  }

  function escapeHtml(s) {
    return (s || '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;','\'':'&#39;'}[c]));
  }

  async function load() {
    const to = toInput.value.trim();
    const url = to ? `/api/emails?to=${encodeURIComponent(to)}` : '/api/emails';
    const res = await fetch(url);
    const data = await res.json();
    render(data);
    startStream();
  }

  function startStream() {
    if (es) { try { es.close(); } catch {} es = null; }
    const to = toInput.value.trim();
    const url = to ? `/api/emails/stream?to=${encodeURIComponent(to)}` : '/api/emails/stream';
    es = new EventSource(url);
    es.onmessage = (e) => {
      try { addCard(JSON.parse(e.data)); } catch {}
    };
    es.addEventListener('message', (e) => {
      try { addCard(JSON.parse(e.data)); } catch {}
    });
  }

  loadBtn.addEventListener('click', load);
})();
