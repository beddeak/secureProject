type EmptyTableRowProps = {
  colSpan: number
  message: string
}

export default function EmptyTableRow({
  colSpan,
  message,
}: EmptyTableRowProps) {
  return (
    <tr>
      <td className="empty-cell" colSpan={colSpan}>
        {message}
      </td>
    </tr>
  )
}
