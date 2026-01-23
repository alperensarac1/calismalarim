import * as SQLite from "expo-sqlite";

export type DbRow = {
    local_id: number;
    external_id: string | null;
    data_json: string;
};

const db = SQLite.openDatabaseSync("csv_explorer_rn.db");

export function initDb() {
    db.execSync(`
    CREATE TABLE IF NOT EXISTS rows (
      local_id INTEGER PRIMARY KEY AUTOINCREMENT,
      external_id TEXT,
      data_json TEXT NOT NULL
    );
  `);
}

export function clearDb() {
    db.execSync(`DELETE FROM rows;`);
}

export function insertAll(rows: { externalId?: string | null; dataJson: string }[]) {
    db.withTransactionSync(() => {
        const stmt = db.prepareSync("INSERT INTO rows (external_id, data_json) VALUES (?, ?)");
        try {
            for (const r of rows) {
                stmt.executeSync([r.externalId ?? null, r.dataJson]);
            }
        } finally {
            stmt.finalizeSync();
        }
    });
}

export function getAll(): DbRow[] {
    return db.getAllSync<DbRow>("SELECT * FROM rows ORDER BY local_id DESC;");
}

export function searchAllColumns(q: string): DbRow[] {
    return db.getAllSync<DbRow>(
        "SELECT * FROM rows WHERE data_json LIKE ? ORDER BY local_id DESC;",
        [`%${q}%`]
    );
}

// JSON string içinde "key":"...q..." araması (basit LIKE)
export function searchInColumn(key: string, q: string): DbRow[] {
    const pattern = `%"${key}":"%${q}%"%`;
    return db.getAllSync<DbRow>(
        "SELECT * FROM rows WHERE data_json LIKE ? ORDER BY local_id DESC;",
        [pattern]
    );
}
