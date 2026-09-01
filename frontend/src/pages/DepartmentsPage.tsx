import { useEffect, useMemo, useState } from 'react'
import { ApiError } from '../api/http'
import EmptyTableRow from '../components/EmptyTableRow'
import PageHeader from '../components/PageHeader'
import { departmentApi } from '../features/departments/departmentApi'
import type {
  Department,
  DepartmentRank,
} from '../features/departments/types'

export default function DepartmentsPage() {
  const [departments, setDepartments] = useState<Department[]>([])
  const [ranks, setRanks] = useState<DepartmentRank[]>([])
  const [selectedDepartmentId, setSelectedDepartmentId] = useState<
    number | null
  >(null)
  const [searchText, setSearchText] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isRankLoading, setIsRankLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [rankErrorMessage, setRankErrorMessage] = useState<string | null>(null)

  useEffect(() => {
    let isCurrent = true

    departmentApi
      .getDepartments()
      .then((response) => {
        if (!isCurrent) return
        setDepartments(response)
        setSelectedDepartmentId((currentId) => currentId ?? response[0]?.id ?? null)
      })
      .catch((error) => {
        if (!isCurrent) return
        setErrorMessage(
          error instanceof ApiError
            ? error.message
            : '부서 정보를 불러오지 못했습니다.',
        )
      })
      .finally(() => {
        if (isCurrent) setIsLoading(false)
      })

    return () => {
      isCurrent = false
    }
  }, [])

  useEffect(() => {
    if (selectedDepartmentId === null) {
      setRanks([])
      return
    }

    let isCurrent = true
    setIsRankLoading(true)
    setRankErrorMessage(null)

    departmentApi
      .getDepartmentRanks(selectedDepartmentId)
      .then((response) => {
        if (isCurrent) {
          setRanks([...response].sort((a, b) => a.levelOrder - b.levelOrder))
        }
      })
      .catch((error) => {
        if (!isCurrent) return
        setRankErrorMessage(
          error instanceof ApiError
            ? error.message
            : '부서 계급을 불러오지 못했습니다.',
        )
      })
      .finally(() => {
        if (isCurrent) setIsRankLoading(false)
      })

    return () => {
      isCurrent = false
    }
  }, [selectedDepartmentId])

  const filteredDepartments = useMemo(() => {
    const normalizedSearch = searchText.trim().toLowerCase()
    if (!normalizedSearch) return departments

    return departments.filter(
      (department) =>
        department.code.toLowerCase().includes(normalizedSearch) ||
        department.name.toLowerCase().includes(normalizedSearch) ||
        department.description.toLowerCase().includes(normalizedSearch),
    )
  }, [departments, searchText])

  const selectedDepartment = departments.find(
    (department) => department.id === selectedDepartmentId,
  )

  return (
    <section>
      <PageHeader
        eyebrow="ORGANIZATION DIRECTORY"
        title="부서 정보"
        description="조직별 업무 범위와 계급 체계를 확인합니다."
      />

      <div className="directory-toolbar">
        <input
          type="search"
          value={searchText}
          onChange={(event) => setSearchText(event.target.value)}
          placeholder="부서 코드, 이름 또는 업무 검색"
        />
        <span>등록 부서 {departments.length}개</span>
      </div>

      {errorMessage ? (
        <div className="inline-alert error" role="alert">
          {errorMessage}
        </div>
      ) : null}

      <div className="table-wrap">
        <table className="department-table">
          <thead>
            <tr>
              <th>부서 코드</th>
              <th>부서명</th>
              <th>업무 설명</th>
              <th>계급 체계</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <EmptyTableRow colSpan={4} message="부서를 불러오는 중입니다." />
            ) : null}
            {!isLoading && !errorMessage && filteredDepartments.length === 0 ? (
              <EmptyTableRow colSpan={4} message="조건에 맞는 부서가 없습니다." />
            ) : null}
            {!isLoading
              ? filteredDepartments.map((department) => (
                  <tr
                    key={department.id}
                    className={
                      selectedDepartmentId === department.id
                        ? 'selected-row'
                        : undefined
                    }
                  >
                    <td>
                      <span className="department-code">{department.code}</span>
                    </td>
                    <td>{department.name}</td>
                    <td className="description-cell">{department.description}</td>
                    <td>
                      <button
                        className="row-action"
                        type="button"
                        onClick={() => setSelectedDepartmentId(department.id)}
                        aria-pressed={selectedDepartmentId === department.id}
                      >
                        {selectedDepartmentId === department.id
                          ? '선택됨'
                          : '계급 보기'}
                      </button>
                    </td>
                  </tr>
                ))
              : null}
          </tbody>
        </table>
      </div>

      {selectedDepartment ? (
        <section className="rank-directory" aria-labelledby="rank-directory-title">
          <header className="rank-directory-header">
            <div>
              <p className="eyebrow">DEPARTMENT RANKS</p>
              <h2 id="rank-directory-title">{selectedDepartment.name} 계급</h2>
            </div>
            <span>{ranks.length}개 계급</span>
          </header>

          {rankErrorMessage ? (
            <div className="inline-alert error" role="alert">
              {rankErrorMessage}
            </div>
          ) : null}

          <div className="table-wrap">
            <table className="rank-table">
              <thead>
                <tr>
                  <th>Level</th>
                  <th>계급 코드</th>
                  <th>계급명</th>
                  <th>설명</th>
                </tr>
              </thead>
              <tbody>
                {isRankLoading ? (
                  <EmptyTableRow colSpan={4} message="계급을 불러오는 중입니다." />
                ) : null}
                {!isRankLoading && !rankErrorMessage && ranks.length === 0 ? (
                  <EmptyTableRow colSpan={4} message="등록된 계급이 없습니다." />
                ) : null}
                {!isRankLoading
                  ? ranks.map((rank) => (
                      <tr key={rank.id}>
                        <td>
                          <span className={`clearance-badge level-${rank.levelOrder}`}>
                            Level-{rank.levelOrder}
                          </span>
                        </td>
                        <td>
                          <span className="department-code">{rank.code}</span>
                        </td>
                        <td>{rank.name}</td>
                        <td className="description-cell">{rank.description}</td>
                      </tr>
                    ))
                  : null}
              </tbody>
            </table>
          </div>
        </section>
      ) : null}
    </section>
  )
}
