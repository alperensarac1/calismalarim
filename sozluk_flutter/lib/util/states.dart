class EntriesUiState {
  final bool loading;
  final String? error;

  const EntriesUiState({
    this.loading = false,
    this.error,
  });

  EntriesUiState copyWith({bool? loading, String? error}) {
    return EntriesUiState(
      loading: loading ?? this.loading,
      error: error,
    );
  }
}

class AddEntryUiState {
  final bool loading;
  final String? error;

  const AddEntryUiState({
    this.loading = false,
    this.error,
  });

  AddEntryUiState copyWith({bool? loading, String? error}) {
    return AddEntryUiState(
      loading: loading ?? this.loading,
      error: error,
    );
  }
}

class EntryDetailUiState {
  final bool loadingEntry;
  final bool loadingComments;
  final bool posting;
  final String? error;

  const EntryDetailUiState({
    this.loadingEntry = false,
    this.loadingComments = false,
    this.posting = false,
    this.error,
  });

  EntryDetailUiState copyWith({
    bool? loadingEntry,
    bool? loadingComments,
    bool? posting,
    String? error,
  }) {
    return EntryDetailUiState(
      loadingEntry: loadingEntry ?? this.loadingEntry,
      loadingComments: loadingComments ?? this.loadingComments,
      posting: posting ?? this.posting,
      error: error,
    );
  }
}
