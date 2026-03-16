'use client';

import { useState, useEffect, useCallback } from 'react';
import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { api, AccountMapping } from '@/lib/api';

export default function MappingsPage() {
  const [mappings, setMappings] = useState<AccountMapping[]>([]);
  const [loading, setLoading] = useState(true);

  const loadMappings = useCallback(async () => {
    try {
      const data = await api.mappings.list();
      setMappings(data);
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadMappings(); }, [loadMappings]);

  const handleVerify = async (id: number) => {
    try {
      const updated = await api.mappings.verify(id);
      setMappings((prev) => prev.map((m) => (m.id === id ? updated : m)));
    } catch {
      // ignore
    }
  };

  const jiraMappings = mappings.filter((m) => m.accountType === 'JIRA');
  const githubMappings = mappings.filter((m) => m.accountType === 'GITHUB');
  const unverified = mappings.filter((m) => !m.verified);

  return (
    <div>
      <Header title="Account Mappings" />
      <div className="p-6 space-y-6">
        {unverified.length > 0 && (
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg px-4 py-3 text-sm text-yellow-800">
            ⚠️ {unverified.length} account mapping{unverified.length > 1 ? 's' : ''} need verification.
          </div>
        )}

        {loading ? (
          <p className="text-gray-500">Loading mappings...</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <div className="flex items-center gap-2">
                  <h3 className="font-semibold text-gray-800">Jira Accounts</h3>
                  <Badge variant="info">{jiraMappings.length}</Badge>
                </div>
              </CardHeader>
              <CardBody>
                {jiraMappings.length === 0 ? (
                  <p className="text-sm text-gray-500">No Jira mappings.</p>
                ) : (
                  <ul className="divide-y divide-gray-100">
                    {jiraMappings.map((m) => (
                      <li key={m.id} className="py-3 flex items-center justify-between gap-4">
                        <div>
                          <p className="text-sm font-medium text-gray-800">{m.staff.name}</p>
                          <p className="text-xs text-gray-500">{m.externalUsername ?? m.externalAccountId}</p>
                        </div>
                        <div className="flex items-center gap-2">
                          <Badge variant={m.verified ? 'success' : 'warning'}>
                            {m.verified ? 'Verified' : 'Unverified'}
                          </Badge>
                          {!m.verified && (
                            <button
                              onClick={() => handleVerify(m.id)}
                              className="text-xs text-blue-600 hover:text-blue-800 font-medium"
                            >
                              Verify
                            </button>
                          )}
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </CardBody>
            </Card>

            <Card>
              <CardHeader>
                <div className="flex items-center gap-2">
                  <h3 className="font-semibold text-gray-800">GitHub Accounts</h3>
                  <Badge variant="default">{githubMappings.length}</Badge>
                </div>
              </CardHeader>
              <CardBody>
                {githubMappings.length === 0 ? (
                  <p className="text-sm text-gray-500">No GitHub mappings.</p>
                ) : (
                  <ul className="divide-y divide-gray-100">
                    {githubMappings.map((m) => (
                      <li key={m.id} className="py-3 flex items-center justify-between gap-4">
                        <div>
                          <p className="text-sm font-medium text-gray-800">{m.staff.name}</p>
                          <p className="text-xs text-gray-500">@{m.externalUsername ?? m.externalAccountId}</p>
                        </div>
                        <div className="flex items-center gap-2">
                          <Badge variant={m.verified ? 'success' : 'warning'}>
                            {m.verified ? 'Verified' : 'Unverified'}
                          </Badge>
                          {!m.verified && (
                            <button
                              onClick={() => handleVerify(m.id)}
                              className="text-xs text-blue-600 hover:text-blue-800 font-medium"
                            >
                              Verify
                            </button>
                          )}
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </CardBody>
            </Card>
          </div>
        )}
      </div>
    </div>
  );
}
