---
description: Review another computer's PR with Fable; approve it or request changes
argument-hint: "<PR number>"
---

Review PR #$ARGUMENTS by the rules in `CLAUDE.md`:

1. Read the description and the diff with `gh pr view $ARGUMENTS` and `gh pr diff $ARGUMENTS`.
   If the PR was opened by this computer's account (`gh api user --jq .login`), stop: you cannot
   approve your own PR.
2. Check that the target branch is `main`, that the source branch name follows the rules and that
   the self-review section of the PR description is filled in. If not, request changes.
3. If the working tree is clean, switch to the branch with `gh pr checkout $ARGUMENTS`, run the tests
   and lint if any, and switch back to the previous branch when done. If the tree is not clean, do
   not check out; continue with the diff only.
4. Review the diff from start to end: correctness, security, error handling, readability, tests.
5. Decide:
   - No blocking problem: `gh pr review $ARGUMENTS --approve --body "<short review summary>"`
   - A blocking problem: `gh pr review $ARGUMENTS --request-changes --body "<problems, with file:line>"`
   - Put small, non-blocking suggestions in the approval body.
   - If you are not sure, do not approve; explain the situation to the user and ask.
   - For a PR with an architectural decision (see `PROJECT_MEMORY.md` §6), ask the user before
     approving.
   - Every review body is written in **English**. No AI tool name or signature (#44).
6. Summarise your decision and its reason for the user.
