# healmeout — Team rules

Three developers work in this repo from three computers. The main AI assistant on each computer is
**Fable 5.2**; the model is pinned in `.claude/settings.json`. These rules apply to people and AI alike.

Project rules, hackathon rules and the GitHub working method (issues, PR approval, comments) are in
the project memory:

@PROJECT_MEMORY.md

## Fixed rules

- **No direct push to `main`.** The GitHub ruleset (`main-koruma`) blocks it on the server for
  everyone, including the repo owner. Nobody has bypass rights.
- **No force push and no deleting `main`.** No force push on any branch (including `--force-with-lease`
  and `+refspec`). So a branch that is already on GitHub is never rebased (see §1).
- **The project language is 100% English:** code, comments, documents, commits, branch names, issues,
  PRs and review comments are written in English. No Turkish documents. The AI assistant talks with
  its own user in Turkish.
- **The AI tool's name is not written to GitHub (#44):** no tool name, signature, co-author line or
  "Generated with …" line in commits, PRs, issues or comments. AI use is declared in the submission form.
- Every change goes this way: **branch → self-review → PR → squash merge.**
- Working around the rules is forbidden: `gh pr merge --admin`, disabling or changing the ruleset,
  `--no-verify`. Changes to the rule files (`CLAUDE.md`, `PROJECT_MEMORY.md`, `.claude/`, `.github/`)
  go through the normal PR process too.

## Branches

- Open a new branch from an up-to-date `main` for every task:
  ```sh
  git switch main && git pull --ff-only
  git switch -c <type>/<topic>
  ```
- Branch names: `feat/<module>`, `fix/<topic>`, `docs/<topic>` (e.g. `feat/scenario-simulator`).
- One branch = one topic. Keep PRs small.
- Do not push to someone else's branch without asking its owner.
- Commit messages in English: `type: short description` (e.g. `fix: reject empty email`).

## 1. Self-review (before opening a PR, required)

The Fable on the computer that opens the PR does not open it without reviewing its own change.
Shortcut: **`/pr-open`**.

1. Bring `main` into the branch: `git fetch origin`, then
   - if the branch has not been pushed yet, `git rebase origin/main`;
   - if the branch is on GitHub, `git merge origin/main` (a rebase rewrites history and needs a force
     push; the squash merge reduces the branch to one commit anyway).
2. Read `git diff origin/main...HEAD` from start to end: bugs, security holes, missing error
   handling, unneeded or dead code, missing tests.
3. Run the project's tests and lint, if any.
4. Fix what you find; if something cannot be fixed, do not open the PR and tell the user.
5. Fill in the PR description's "Self-review" section in English (`.github/pull_request_template.md`).

## 2. Review

- The ruleset requires no approval; the approval and review rules are in `PROJECT_MEMORY.md` §6.
- Shortcut: **`/pr-review <PR number>`**. The AI reads the diff, runs the tests if it can, and gives
  **Request changes** if it finds a problem.

## 3. Merge

- **Squash merge** only. The PR owner merges:
  ```sh
  gh pr merge <number> --squash --delete-branch
  ```
- After merging: `git switch main && git pull --ff-only`

## Setup (once per computer)

1. `gh auth login`: each person with their own GitHub account.
2. Clone the repo and start the AI assistant in the repo root. The model comes from
   `.claude/settings.json`; do not change it with `/model`.
3. `.claude/hooks/main-push-guard.sh` stops the AI's attempts to push to `main` locally; the real
   protection is the GitHub ruleset.

The ruleset is kept in `.github/rulesets/main-koruma.json` (the name is the live ruleset's name on
GitHub). After a change is merged by PR, a repo admin applies it to GitHub with
`scripts/apply-ruleset.sh`.
