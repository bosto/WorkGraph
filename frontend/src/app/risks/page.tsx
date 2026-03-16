'use client';

import { useState, useEffect, useCallback } from 'react';
import { Header } from '@/components/layout/Header';
import { Card, CardBody } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { api, RiskAlert } from '@/lib/api';

export default function RisksPage() {
  const [alerts, setAlerts] = useState<RiskAlert[]>([]);
  const [loading, setLoading] = useState(true);
  const [scanning, setScanning] = useState(false);

  const loadAlerts = useCallback(async () => {
    try {
      const data = await api.risks.list();
      setAlerts(data);
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadAlerts(); }, [loadAlerts]);

  const handleResolve = async (id: number) => {
    try {
      await api.risks.resolve(id);
      setAlerts((prev) => prev.filter((a) => a.id !== id));
    } catch {
      // ignore
    }
  };

  const handleScan = async () => {
    setScanning(true);
    try {
      await api.risks.scan();
      await loadAlerts();
    } catch {
      // ignore
    } finally {
      setScanning(false);
    }
  };

  const grouped = alerts.reduce<Record<string, RiskAlert[]>>((acc, alert) => {
    const key = alert.alertType;
    acc[key] = acc[key] ?? [];
    acc[key].push(alert);
    return acc;
  }, {});

  return (
    <div>
      <Header title="Risk Alerts" />
      <div className="p-6 space-y-6">
        <div className="flex items-center justify-between">
          <div className="flex gap-4">
            <div className="text-center">
              <p className="text-2xl font-bold text-red-600">{alerts.filter((a) => a.severity === 'HIGH').length}</p>
              <p className="text-xs text-gray-500">High</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-bold text-yellow-600">{alerts.filter((a) => a.severity === 'MEDIUM').length}</p>
              <p className="text-xs text-gray-500">Medium</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-bold text-gray-600">{alerts.filter((a) => a.severity === 'LOW').length}</p>
              <p className="text-xs text-gray-500">Low</p>
            </div>
          </div>
          <button
            onClick={handleScan}
            disabled={scanning}
            className="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 disabled:opacity-50"
          >
            {scanning ? 'Scanning...' : 'Run Risk Scan'}
          </button>
        </div>

        {loading ? (
          <p className="text-gray-500">Loading alerts...</p>
        ) : alerts.length === 0 ? (
          <Card><CardBody><p className="text-gray-500 text-sm">No active risk alerts. 🎉</p></CardBody></Card>
        ) : (
          Object.entries(grouped).map(([type, typeAlerts]) => (
            <Card key={type}>
              <div className="px-6 py-3 border-b border-gray-200 flex items-center justify-between">
                <h3 className="font-semibold text-gray-800">{type.replace(/_/g, ' ')}</h3>
                <Badge variant="default">{typeAlerts.length}</Badge>
              </div>
              <CardBody>
                <ul className="divide-y divide-gray-100">
                  {typeAlerts.map((alert) => (
                    <li key={alert.id} className="py-3 flex items-start justify-between gap-4">
                      <div className="flex-1">
                        <p className="text-sm font-medium text-gray-800">{alert.title}</p>
                        {alert.detail && <p className="text-xs text-gray-500 mt-0.5">{alert.detail}</p>}
                        <div className="flex gap-2 mt-1">
                          {alert.staff && <span className="text-xs text-gray-400">👤 {alert.staff.name}</span>}
                          {alert.project && <span className="text-xs text-gray-400">📁 {alert.project.name}</span>}
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <Badge variant={alert.severity === 'HIGH' ? 'danger' : alert.severity === 'MEDIUM' ? 'warning' : 'default'}>
                          {alert.severity}
                        </Badge>
                        <button
                          onClick={() => handleResolve(alert.id)}
                          className="text-xs text-green-600 hover:text-green-800 font-medium"
                        >
                          Resolve
                        </button>
                      </div>
                    </li>
                  ))}
                </ul>
              </CardBody>
            </Card>
          ))
        )}
      </div>
    </div>
  );
}
