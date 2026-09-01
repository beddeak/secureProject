import EmptyTableRow from '../components/EmptyTableRow'
import PageHeader from '../components/PageHeader'

export default function PersonnelPage() {
  return (
    <section>
      <PageHeader
        eyebrow="PERSONNEL CONTROL"
        title="직원 관리"
        description="부서의 활성 구성원과 현재 계급을 확인합니다."
        actions={
          <button className="primary-button" type="button" disabled>
            직원 배정
          </button>
        }
      />

      <div className="filter-bar compact">
        <select defaultValue="">
          <option value="" disabled>부서 선택</option>
        </select>
        <input type="search" placeholder="닉네임 검색" />
      </div>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>사용자 ID</th>
              <th>닉네임</th>
              <th>직책</th>
              <th>부서 계급</th>
              <th>인가 등급</th>
              <th>가입일</th>
            </tr>
          </thead>
          <tbody>
            <EmptyTableRow colSpan={6} message="표시할 활성 구성원이 없습니다." />
          </tbody>
        </table>
      </div>
    </section>
  )
}
