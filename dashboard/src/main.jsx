import React, { useEffect, useState } from "react";
import { createRoot } from "react-dom/client";

const api = import.meta.env.VITE_API_URL || "http://localhost:8080";

function App() {
  const [incidents, setIncidents] = useState([]);
  const [metrics, setMetrics] = useState({});
  const [scenario, setScenario] = useState(null);
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    Promise.all([
      fetch(`${api}/api/incidents`).then(r => r.json()),
      fetch(`${api}/api/metrics`).then(r => r.json()),
      fetch(`${api}/api/demo/scenario`).then(r => r.json()),
    ]).then(([nextIncidents, nextMetrics, nextScenario]) => {
      setIncidents(nextIncidents);
      setMetrics(nextMetrics);
      setScenario(nextScenario);
      setSelected(nextIncidents[0] || null);
    }).catch(() => {
      setScenario({ name: "Demo unavailable: start the API", normalLatencyMs: 150, spikeLatencyMs: 2000 });
    });
  }, []);

  return (
    <main style={{ fontFamily: "Inter, system-ui", margin: 0, background: "#0b1020", color: "#e7edf7", minHeight: "100vh" }}>
      <header style={{ padding: "20px 28px", borderBottom: "1px solid #26324a" }}>
        <h1 style={{ margin: 0 }}>AIOps Incident Center</h1>
        <small>MTTD / MTTR intelligence console</small>
      </header>
      <section style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 12, padding: 20 }}>
        <Metric label="Active incidents" value={metrics.activeIncidents ?? 0} />
        <Metric label="MTTD" value={`${Math.round(metrics.mttdSeconds ?? 0)} sec`} />
        <Metric label="MTTR" value={`${Math.round(metrics.mttrSeconds ?? 0)} sec`} />
        <Metric label="Detection accuracy" value={`${Math.round((metrics.detectionAccuracy ?? 0) * 100)}%`} />
      </section>
      <section style={{ display: "grid", gridTemplateColumns: "320px 1fr", gap: 16, padding: "0 20px 20px" }}>
        <aside style={{ background: "#11182b", borderRadius: 10, padding: 12 }}>
          <h2>Incidents</h2>
          {incidents.map(incident => (
            <button key={incident.id} onClick={() => setSelected(incident)}
              style={{ width: "100%", textAlign: "left", marginBottom: 8, padding: 12, borderRadius: 8,
                border: selected?.id === incident.id ? "1px solid #64d2ff" : "1px solid #26324a",
                background: "#18213a", color: "#fff", cursor: "pointer" }}>
              <strong>{incident.title}</strong><br /><small>{incident.id} · {incident.status}</small>
            </button>
          ))}
        </aside>
        <article style={{ background: "#11182b", borderRadius: 10, padding: 20 }}>
          {selected ? <>
            <h2>{selected.title}</h2>
            <p>{selected.description}</p>
            <p><b>Service:</b> {selected.affectedService} · <b>Severity:</b> {selected.severity}</p>
            <h3>Timeline</h3>
            <ul>{selected.timeline?.map((entry, index) => <li key={index}>{entry}</li>)}</ul>
            <h3>Root causes</h3>
            <ul>{selected.rootCauses?.map((cause, index) => <li key={index}>{cause}</li>)}</ul>
            <h3>Remediation recommendations</h3>
            {selected.recommendations?.map((item, index) => (
              <div key={index} style={{ borderLeft: "3px solid #64d2ff", padding: "4px 12px", marginBottom: 10 }}>
                <strong>{item.title}</strong> · confidence {Math.round(item.confidence * 100)}%
                <p>{item.description}</p>
              </div>
            ))}
          </> : <p>Select an incident.</p>}
        </article>
      </section>
      {scenario && <section style={{ padding: "0 20px 20px" }}>
        <div style={{ background: "#11182b", borderRadius: 10, padding: 16 }}>
          <b>Seed scenario:</b> {scenario.name} · normal latency {scenario.normalLatencyMs} ms · spike {scenario.spikeLatencyMs} ms
        </div>
      </section>}
    </main>
  );
}

function Metric({ label, value }) {
  return <div style={{ background: "#11182b", borderRadius: 10, padding: 16 }}>
    <small>{label}</small><div style={{ fontSize: 28, fontWeight: 700, marginTop: 6 }}>{value}</div>
  </div>;
}

createRoot(document.getElementById("root")).render(<App />);
