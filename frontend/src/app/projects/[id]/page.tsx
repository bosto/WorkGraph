import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { StatCard } from '@/components/ui/StatCard';
import { api } from '@/lib/api';
import { notFound } from 'next/navigation';

async function getProjectData(id: number) {
  try {
    const [project, repos, jiraIssues, prs, workItems, risks] = await Promise.allSettled([
      api.projects.get(id),
      api.projects.repos(id),
      api.jira.issues({ projectId: id }),
      api.github.prs({ projectId: id }),
      api.workItems.list({ projectId: id }),
      api.risks.list({ projectId: id }),
    ]);
    return {
      project: project.status === 'fulfilled' ? project.value : null,
      repos: repos.status === 'fulfilled' ? repos.value : [],
      jiraIssues: jiraIssues.status === 'fulfilled' ? jiraIssues.value : [],
      prs: prs.status === 'fulfilled' ? prs.value : [],
      workItems: workItems.status === 'fulfilled' ? workItems.value : [],
      risks: risks.status === 'fulfilled' ? risks.value : [],
    };
  } catch {
    return null;
  }
}

export default async function ProjectDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id: idStr } = await params;
  const id = parseInt(idStr);
  const data = await getProjectData(id);

  if (!data || !data.project) return notFound();

  const { project, repos, jiraIssues, prs, workItems, risks } = data;
  const openPRs = prs.filter((p) => p.state === 'open');
  const openIssues = jiraIssues.filter(
    (i) => i.status && !['Done', 'Resolved', 'Closed'].includes(i.status)
  );

  return (
    <div>
      <Header title={project.name} />
      <div className="p-6 space-y-6">
        <Card>
          <CardBody>
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-gray-900">{project.name}</h3>
                <p className="text-sm font-mono text-gray-500">{project.key}</p>
                {project.description && <p className="text-sm text-gray-600 mt-2">{project.description}</p>}
              </div>
              <Badge variant={project.active ? 'success' : 'default'}>{project.active ? 'Active' : 'Inactive'}</Badge>
            </div>
            {project.jiraProjectKey && (
              <div className="mt-3 text-sm">
                <span className="text-gray-500">Jira Project:</span>{' '}
                <span className="font-mono font-medium">{project.jiraProjectKey}</span>
              </div>
            )}
          </CardBody>
        </Card>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <StatCard label="Jira Issues" value={jiraIssues.length} sub={`${openIssues.length} open`} />
          <StatCard label="Pull Requests" value={prs.length} sub={`${openPRs.length} open`} />
          <StatCard label="Work Items" value={workItems.length} />
          <StatCard label="Risk Alerts" value={risks.length} />
        </div>

        {repos.length > 0 && (
          <Card>
            <CardHeader>
              <h3 className="font-semibold text-gray-800">GitHub Repositories</h3>
            </CardHeader>
            <CardBody>
              <ul className="divide-y divide-gray-100">
                {repos.map((r) => (
                  <li key={r.id} className="py-2">
                    <a
                      href={`https://github.com/${r.githubOwner}/${r.githubRepo}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-sm text-blue-600 hover:underline font-mono"
                    >
                      {r.githubOwner}/{r.githubRepo}
                    </a>
                  </li>
                ))}
              </ul>
            </CardBody>
          </Card>
        )}

        {jiraIssues.length > 0 && (
          <Card>
            <CardHeader>
              <h3 className="font-semibold text-gray-800">Jira Issues</h3>
            </CardHeader>
            <CardBody>
              <div className="overflow-x-auto">
                <table className="min-w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-200">
                      <th className="text-left py-2 px-2 font-medium text-gray-600">Key</th>
                      <th className="text-left py-2 px-2 font-medium text-gray-600">Summary</th>
                      <th className="text-left py-2 px-2 font-medium text-gray-600">Assignee</th>
                      <th className="text-left py-2 px-2 font-medium text-gray-600">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {jiraIssues.slice(0, 10).map((issue) => (
                      <tr key={issue.id} className="hover:bg-gray-50">
                        <td className="py-2 px-2 font-mono text-xs text-blue-600">{issue.key}</td>
                        <td className="py-2 px-2 text-gray-700 max-w-xs truncate">{issue.summary}</td>
                        <td className="py-2 px-2 text-gray-600 text-xs">{issue.assigneeStaff?.name ?? 'Unassigned'}</td>
                        <td className="py-2 px-2">
                          <Badge variant={
                            issue.status === 'Done' || issue.status === 'Resolved' ? 'success'
                            : issue.status === 'In Progress' ? 'info'
                            : 'default'
                          }>{issue.status ?? '-'}</Badge>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </CardBody>
          </Card>
        )}

        {prs.length > 0 && (
          <Card>
            <CardHeader>
              <h3 className="font-semibold text-gray-800">Pull Requests</h3>
            </CardHeader>
            <CardBody>
              <ul className="divide-y divide-gray-100">
                {prs.slice(0, 8).map((pr) => (
                  <li key={pr.id} className="py-2 flex items-center justify-between gap-4">
                    <div>
                      <p className="text-sm font-medium text-gray-800">{pr.title}</p>
                      <p className="text-xs text-gray-400">
                        #{pr.number} by {pr.authorStaff?.name ?? 'Unknown'}
                      </p>
                    </div>
                    <Badge variant={pr.state === 'open' ? 'info' : pr.mergedAt ? 'success' : 'default'}>
                      {pr.state}
                    </Badge>
                  </li>
                ))}
              </ul>
            </CardBody>
          </Card>
        )}

        {risks.length > 0 && (
          <Card>
            <CardHeader>
              <h3 className="font-semibold text-gray-800">Risk Alerts</h3>
            </CardHeader>
            <CardBody>
              <ul className="divide-y divide-gray-100">
                {risks.map((r) => (
                  <li key={r.id} className="py-2 flex items-center justify-between gap-4">
                    <div>
                      <p className="text-sm font-medium text-gray-800">{r.title}</p>
                      {r.detail && <p className="text-xs text-gray-500">{r.detail}</p>}
                    </div>
                    <Badge variant={r.severity === 'HIGH' ? 'danger' : r.severity === 'MEDIUM' ? 'warning' : 'default'}>
                      {r.severity}
                    </Badge>
                  </li>
                ))}
              </ul>
            </CardBody>
          </Card>
        )}
      </div>
    </div>
  );
}
