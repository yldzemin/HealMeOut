---
description: Self-review, push the branch and open a PR
argument-hint: "[PR title]"
---

Run the PR process for the current branch by the rules in `CLAUDE.md`:

1. If the current branch is `main`, stop: first open a branch named `feat/<module>`, `fix/<topic>`
   or `docs/<topic>`.
2. If there are uncommitted changes, ask the user what to do with them.
3. `git fetch origin`. If the branch has not been pushed yet, `git rebase origin/main`; if it is on
   GitHub, `git merge origin/main` (no force push; a pushed branch is never rebased). Resolve any
   conflict together with the user.
4. Self-review: read the output of `git diff origin/main...HEAD` from start to end. Look for bugs,
   security holes, missing error handling, unneeded or dead code and missing tests. Run the
   project's tests and lint, if any.
5. Fix and commit what you find. If a blocking problem cannot be fixed, do not open the PR: tell the
   user and stop.
6. `git push -u origin HEAD`
7. Open the PR with `gh pr create --base main`. Title: $ARGUMENTS (if empty, derive it from the
   change). Title and body are written in **English**. Fill in the body from
   `.github/pull_request_template.md`; in the "Self-review" section, say what you checked, what you
   found and what you fixed. Add `Closes #N` for the related issue, if any.
8. Give the PR link. Say that it is merged with `gh pr merge <number> --squash --delete-branch`, and
   that a review from another computer can be had with `/pr-review <number>` if wanted.
