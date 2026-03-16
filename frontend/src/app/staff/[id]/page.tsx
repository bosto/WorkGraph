import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { StatCard } from '@/components/ui/StatCard';
import { api } from '@/lib/api';
import { notFound } from 'next/navigation';

async function getStaffData(id: number) {
  try {
    const [member, mappings, jiraIssues, prs, workItems, risks] = await Promise.allSettled([
      api.staff.get(id),
      api.mappings.forStaff(id),
      api.jira.issues({ staffId: id }),
      api.github.prs({ staffId: id }),
      api.workItems.list({ staffId: id }),
      api.risks.list({ staffId: id }),
    ]);
    return {
      member: member.status === 'fulfilled' ? member.value : null,
      mappings: mappings.status === 'fulfilled' ? mappings.value : [],
      jiraIssues: jiraIssues.status === 'fulfilled' ? jiraIssues.value : [],
      prs: prs.status === 'fulfilled' ? prs.value : [],
      workItems: workItems.status === 'fulfilled' ? workItems.value : [],
      risks: risks.status === 'fulfilled' ? risks.value : [],
    };
  } catch {
    return null;
  }
}

export default async function StaffDetailPage({ params }: { params: { id: string } }) {
  const id = parseInt(params.id);
  const data = await getStaffData(id);

  if (!data || !data.member) return notFound();

  const { member, mappings, jiraIssues, prs, workItems, risks } = data;
  const activeWorkItems = workItems.filter(
    (w) => w.status && !['Done', 'Resolved', 'Closed', 'merged', 'closed'].includes(w.status)
  );
  const openPRs = prs.filter((p) => p.state === 'open');

  return (
    <div>
      <Header title={member.name} />
      <div className="p-6 space-y-6">
        <div className="flex items-start gap-6">
          <div className="flex-1">
            <Card>
              <CardBody>
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">{member.name}</h3>
                    <p className="text-gray-500 text-sm">{member.email}</p>
                    <p className="text-gray-400 text-xs mt-1">{member.employeeId}</p>
                  </div>
                  <Badge variant={member.active ? 'success' : 'default'}>{member.active ? 'Active' : 'Inactive'}</Badge>
                </div>
                <div className="mt-4 grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <span className="text-gray-500">Team:</span>{' '}
                    <span className="font-medium">{member.team ?? '-'}</span>
                  </div>
                  <div>
                    <span className="text-gray-500">Role:</span>{' '}
                    <span className="font-medium">{member.role ?? '-'}</span>
                  </div>
                </div>
              </CardBody>
            </Card>
          </div>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <StatCard label="Active Work Items" value={activeWorkItems.length} />
          <StatCard label="Jira Issues" value={jiraIssues.length} />
          <StatCard label="Pull Requests" value={prs.length} sub={`${openPRs.length} open`} />
          <StatCard label="Risk Alerts" value={risks.length} />
        </div>

        {mappings.length > 0 && (
          <Card>
            <CardHeader>
              <h3 className="font-semibold text-gray-800">Account Mappings</h3>
            </CardHeader>
            <CardBody>
              <ul className="divide-y divide-gray-100">
                {mappings.map((m) => (
                  <li key={m.id} className="py-2 flex items-center justify-between">
                    <div>
                      <Badge variant={m.accountType === 'JIRA' ? 'info' : 'default'}>{m.accountType}</Badge>
                      <span className="ml-2 text-sm text-gray-700">{m.externalUsername ?? m.externalAccountId}</span>
                    </div>
                    <Badge variant={m.verified ? 'success' : 'warning'}>{m.verified ? 'Verified' : 'Unverified'}</Badge>
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
                      <th className="text-left py-2 px-2 font-medium text-gray-600">Status</th>
                      <th className="text-left py-2 px-2 font-medium text-gray-600">Priority</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {jiraIssues.slice(0, 10).map((issue) => (
                      <tr key={issue.id} className="hover:bg-gray-50">
                        <td className="py-2 px-2 font-mono text-xs text-blue-600">{issue.key}</td>
                        <td className="py-2 px-2 text-gray-700 max-w-xs truncate">{issue.summary}</td>
                        <td className="py-2 px-2">
                          <Badge variant={
                            issue.status === 'Done' || issue.status === 'Resolved' ? 'success'
                            : issue.status === 'In Progress' ? 'info'
                            : 'default'
                          }>{issue.status ?? '-'}</Badge>
                        </td>
                        <td className="py-2 px-2 text-gray-600">{issue.priority ?? '-'}</td>
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
                      <p className="text-xs text-gray-400">{pr.githubOwner}/{pr.githubRepo} #{pr.number}</p>
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
