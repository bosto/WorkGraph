const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080/api';

export interface Staff {
  id: number;
  employeeId: string;
  name: string;
  email: string;
  team: string | null;
  role: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AccountMapping {
  id: number;
  staff: Staff;
  accountType: 'JIRA' | 'GITHUB';
  externalAccountId: string;
  externalUsername: string | null;
  verified: boolean;
  createdAt: string;
}

export interface Project {
  id: number;
  key: string;
  name: string;
  description: string | null;
  jiraProjectKey: string | null;
  active: boolean;
}

export interface RepositoryMapping {
  id: number;
  project: Project;
  githubOwner: string;
  githubRepo: string;
}

export interface JiraIssue {
  id: number;
  jiraId: string;
  key: string;
  summary: string;
  status: string | null;
  issueType: string | null;
  priority: string | null;
  storyPoints: number | null;
  updatedAt: string | null;
  project: Project | null;
  assigneeStaff: Staff | null;
}

export interface GitHubPullRequest {
  id: number;
  number: number;
  githubOwner: string;
  githubRepo: string;
  title: string;
  state: string;
  draft: boolean;
  authorStaff: Staff | null;
  project: Project | null;
  createdAt: string | null;
  mergedAt: string | null;
}

export interface GitHubCommit {
  id: number;
  sha: string;
  githubOwner: string;
  githubRepo: string;
  message: string | null;
  authorStaff: Staff | null;
  authoredAt: string | null;
  additions: number;
  deletions: number;
}

export interface WorkItem {
  id: number;
  staff: Staff;
  project: Project | null;
  sourceType: 'JIRA' | 'GITHUB';
  sourceId: number;
  title: string;
  status: string | null;
  itemType: string | null;
  priority: string | null;
  updatedAt: string | null;
}

export interface RiskAlert {
  id: number;
  alertType: string;
  severity: 'HIGH' | 'MEDIUM' | 'LOW';
  staff: Staff | null;
  project: Project | null;
  title: string;
  detail: string | null;
  resolved: boolean;
  detectedAt: string;
}

export interface SyncStatus {
  source: string;
  issueCount?: number;
  commitCount?: number;
  prCount?: number;
  lastSyncAt: string | null;
}

async function fetchApi<T>(path: string): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, { cache: 'no-store' });
  if (!res.ok) throw new Error(`API error: ${res.status} ${path}`);
  return res.json();
}

async function postApi<T>(path: string, body?: unknown): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw new Error(`API error: ${res.status} ${path}`);
  return res.json();
}

export const api = {
  staff: {
    list: () => fetchApi<Staff[]>('/staff'),
    get: (id: number) => fetchApi<Staff>(`/staff/${id}`),
    create: (data: Partial<Staff>) => postApi<Staff>('/staff', data),
  },
  mappings: {
    list: () => fetchApi<AccountMapping[]>('/mappings'),
    unverified: () => fetchApi<AccountMapping[]>('/mappings/unverified'),
    verify: (id: number) => postApi<AccountMapping>(`/mappings/${id}/verify`),
    forStaff: (staffId: number) => fetchApi<AccountMapping[]>(`/mappings/staff/${staffId}`),
  },
  projects: {
    list: () => fetchApi<Project[]>('/projects'),
    get: (id: number) => fetchApi<Project>(`/projects/${id}`),
    repos: (id: number) => fetchApi<RepositoryMapping[]>(`/projects/${id}/repos`),
  },
  jira: {
    issues: (params?: { staffId?: number; projectId?: number }) => {
      const qs = params
        ? '?' + Object.entries(params).filter(([, v]) => v !== undefined).map(([k, v]) => `${k}=${v}`).join('&')
        : '';
      return fetchApi<JiraIssue[]>(`/jira/issues${qs}`);
    },
    status: () => fetchApi<SyncStatus>('/jira/status'),
    sync: () => postApi<string>('/jira/sync'),
  },
  github: {
    prs: (params?: { staffId?: number; projectId?: number }) => {
      const qs = params
        ? '?' + Object.entries(params).filter(([, v]) => v !== undefined).map(([k, v]) => `${k}=${v}`).join('&')
        : '';
      return fetchApi<GitHubPullRequest[]>(`/github/prs${qs}`);
    },
    commits: (params?: { staffId?: number; projectId?: number }) => {
      const qs = params
        ? '?' + Object.entries(params).filter(([, v]) => v !== undefined).map(([k, v]) => `${k}=${v}`).join('&')
        : '';
      return fetchApi<GitHubCommit[]>(`/github/commits${qs}`);
    },
    status: () => fetchApi<SyncStatus>('/github/status'),
    sync: () => postApi<string>('/github/sync'),
  },
  workItems: {
    list: (params?: { staffId?: number; projectId?: number }) => {
      const qs = params
        ? '?' + Object.entries(params).filter(([, v]) => v !== undefined).map(([k, v]) => `${k}=${v}`).join('&')
        : '';
      return fetchApi<WorkItem[]>(`/work-items${qs}`);
    },
  },
  risks: {
    list: (params?: { staffId?: number; projectId?: number }) => {
      const qs = params
        ? '?' + Object.entries(params).filter(([, v]) => v !== undefined).map(([k, v]) => `${k}=${v}`).join('&')
        : '';
      return fetchApi<RiskAlert[]>(`/risks${qs}`);
    },
    resolve: (id: number) => postApi<RiskAlert>(`/risks/${id}/resolve`),
    scan: () => postApi<string>('/risks/scan'),
  },
};
