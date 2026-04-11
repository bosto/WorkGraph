'use client';

import { useState, useEffect, useCallback } from 'react';
import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { api, SyncStatus } from '@/lib/api';

export default function SyncStatusPage() {
  const [jiraStatus, setJiraStatus] = useState<SyncStatus | null>(null);
  const [githubStatus, setGithubStatus] = useState<SyncStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState<'jira' | 'github' | null>(null);

  const loadStatus = useCallback(async () => {
    try {
      const [jira, github] = await Promise.allSettled([
        api.jira.status(),
        api.github.status(),
      ]);
      if (jira.status === 'fulfilled') setJiraStatus(jira.value);
      if (github.status === 'fulfilled') setGithubStatus(github.value);
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadStatus(); }, [loadStatus]);

  const handleSync = async (source: 'jira' | 'github') => {
    setSyncing(source);
    try {
      if (source === 'jira') await api.jira.sync();
      else await api.github.sync();
      await loadStatus();
    } catch {
      // ignore
    } finally {
      setSyncing(null);
    }
  };

  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return 'Never';
    return new Date(dateStr).toLocaleString();
  };

  return (
    <div>
      <Header title="Sync Status" />
      <div className="p-6 space-y-6">
        {loading ? (
          <p className="text-gray-500">Loading sync status...</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <h3 className="font-semibold text-gray-800">Jira Sync</h3>
                  <Badge variant="info">JIRA</Badge>
                </div>
              </CardHeader>
              <CardBody>
                {jiraStatus ? (
                  <dl className="space-y-3 text-sm">
                    <div className="flex justify-between">
                      <dt className="text-gray-500">Issues Synced</dt>
                      <dd className="font-semibold text-gray-900">{jiraStatus.issueCount ?? 0}</dd>
                    </div>
                    <div className="flex justify-between">
                      <dt className="text-gray-500">Last Sync</dt>
                      <dd className="text-gray-700">{formatDate(jiraStatus.lastSyncAt)}</dd>
                    </div>
                  </dl>
                ) : (
                  <p className="text-sm text-gray-500">Not configured</p>
                )}
                <button
                  onClick={() => handleSync('jira')}
                  disabled={syncing === 'jira'}
                  className="mt-4 w-full px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 disabled:opacity-50"
                >
                  {syncing === 'jira' ? 'Syncing...' : 'Sync Now'}
                </button>
              </CardBody>
            </Card>

            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <h3 className="font-semibold text-gray-800">GitHub Sync</h3>
                  <Badge variant="default">GITHUB</Badge>
                </div>
              </CardHeader>
              <CardBody>
                {githubStatus ? (
                  <dl className="space-y-3 text-sm">
                    <div className="flex justify-between">
                      <dt className="text-gray-500">Commits Synced</dt>
                      <dd className="font-semibold text-gray-900">{githubStatus.commitCount ?? 0}</dd>
                    </div>
                    <div className="flex justify-between">
                      <dt className="text-gray-500">PRs Synced</dt>
                      <dd className="font-semibold text-gray-900">{githubStatus.prCount ?? 0}</dd>
                    </div>
                    <div className="flex justify-between">
                      <dt className="text-gray-500">Last Sync</dt>
                      <dd className="text-gray-700">{formatDate(githubStatus.lastSyncAt)}</dd>
                    </div>
                  </dl>
                ) : (
                  <p className="text-sm text-gray-500">Not configured</p>
                )}
                <button
                  onClick={() => handleSync('github')}
                  disabled={syncing === 'github'}
                  className="mt-4 w-full px-4 py-2 bg-gray-800 text-white text-sm rounded-lg hover:bg-gray-900 disabled:opacity-50"
                >
                  {syncing === 'github' ? 'Syncing...' : 'Sync Now'}
                </button>
              </CardBody>
            </Card>
          </div>
        )}
      </div>
    </div>
  );
}
