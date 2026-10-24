package com.grimoire.dnd.data.db

data class ContradictionReport(
    val hasContradiction: Boolean,
    val contradictoryFact: CanonLedgerRecord? = null,
    val reason: String = ""
)

class CanonAuditor {
    /**
     * Checks if a proposed narration or new assertion directly contradicts established canon.
     * Simple semantic and exact negation matching for deterministic consistency.
     */
    fun auditAssertion(newFact: CanonLedgerRecord, establishedCanon: List<CanonLedgerRecord>): ContradictionReport {
        for (existing in establishedCanon) {
            if (existing.subject.equals(newFact.subject, ignoreCase = true)) {
                val existingText = existing.assertionText.lowercase()
                val newText = newFact.assertionText.lowercase()

                // Check for direct antonym / status contradictions
                val statusContradictions = listOf(
                    "dead" to "alive",
                    "destroyed" to "intact",
                    "locked" to "unlocked",
                    "friendly" to "hostile",
                    "sealed" to "open"
                )

                for ((a, b) in statusContradictions) {
                    if ((existingText.contains(a) && newText.contains(b)) ||
                        (existingText.contains(b) && newText.contains(a))) {
                        return ContradictionReport(
                            hasContradiction = true,
                            contradictoryFact = existing,
                            reason = "Subject '${newFact.subject}' contradicts canon: '$existingText' vs '$newText'"
                        )
                    }
                }
            }
        }
        return ContradictionReport(hasContradiction = false)
    }
}
