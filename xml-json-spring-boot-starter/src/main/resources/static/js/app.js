const app = Vue.createApp({
  data(){
    return { page:0, entries:[], totalPages:0, q:'', searchMode:false };
  },
  methods:{
    async load(){
      const r = await fetch(`/audit/api?page=${this.page}`);
      if(r.ok){ this.entries = await r.json(); }
    },
    async search(){
      const r = await fetch(`/v1/audit/search?q=${encodeURIComponent(this.q)}&page=${this.page}&size=20`);
      if(r.ok){ const data = await r.json(); this.entries = data.items; this.totalPages = Math.ceil(data.total/data.size); }
    },
    performSearch(){ this.page=0; this.search(); },
    formatTime(ts){ const d=new Date(ts); return d.toISOString().replace('T',' ').substring(0,19); },
    prev(){ if(this.page>0){ this.page--; this.refresh(); } },
    next(){ if(this.page < this.totalPages-1){ this.page++; this.refresh(); } },
    goto(p){ this.page=p; this.refresh(); },
    refresh(){ this.searchMode ? this.search() : this.load(); }
  },
  mounted(){
    if(document.getElementById('auditList')){ this.load(); }
    if(document.getElementById('searchPage')){ this.searchMode=true; this.search(); }
  }
});
if(document.getElementById('app')){ app.mount('#app'); }

// highlight existing code blocks
if(window.hljs){ document.querySelectorAll('pre code').forEach(el=>hljs.highlightElement(el)); }

// fetch presigned URLs for audit detail
const detail = document.getElementById('auditDetail');
if(detail){
  const id = detail.dataset.entryId;
  fetch(`/audit/${id}/xmlUrl`).then(r=>r.ok?r.text():null).then(u=>{
    if(u){ fetch(u).then(r=>r.text()).then(t=>{ const el=document.getElementById('xmlContent'); el.textContent=t; if(window.hljs) hljs.highlightElement(el); }); }
  });
  fetch(`/audit/${id}/jsonUrl`).then(r=>r.ok?r.text():null).then(u=>{
    if(u){ fetch(u).then(r=>r.text()).then(t=>{ const el=document.getElementById('jsonContent'); el.textContent=t; if(window.hljs) hljs.highlightElement(el); }); }
  });
}

// copy buttons
document.querySelectorAll('[data-copy]').forEach(btn=>{
  btn.addEventListener('click',()=>{
    const target = document.getElementById(btn.getAttribute('data-copy'));
    if(target){ navigator.clipboard.writeText(target.textContent); }
  });
});

// theme toggle
const toggle = document.getElementById('themeToggle');
if(toggle){
  toggle.addEventListener('click',()=>{
    const dark = document.body.getAttribute('data-bs-theme') === 'dark';
    document.body.setAttribute('data-bs-theme', dark ? 'light':'dark');
    toggle.setAttribute('aria-pressed', (!dark).toString());
  });
}
