import 'cell_state.dart';

class BoardCell {
  final int row;
  final int col;
  final CellState state;

  BoardCell({
    required this.row,
    required this.col,
    required this.state,
  });

  BoardCell copyWith({
    int? row,
    int? col,
    CellState? state,
  }) {
    return BoardCell(
      row: row ?? this.row,
      col: col ?? this.col,
      state: state ?? this.state,
    );
  }
}
