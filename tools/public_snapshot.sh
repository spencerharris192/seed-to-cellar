#!/usr/bin/env bash
# Publishes a release to the public repository (github.com/spencerharris192/seed-to-cellar) as one commit on its main
# branch: the files of the current commit minus the private ones below, authored with GitHub's private noreply address
# (never the developer's own email), on top of the previous public release. Development happens in the private
# seed-to-cellar-dev repository (remote "origin"); the public one is remote "public".
#
#   tools/public_snapshot.sh "Seed to Cellar 1.0.0"        # builds the commit on branch "public" and pushes it
set -euo pipefail
message="${1:?usage: tools/public_snapshot.sh \"Seed to Cellar <version>\"}"
cd "$(git rev-parse --show-toplevel)"

# Kept private: working notes, design documents, this machine's launcher and the store-page materials.
PRIVATE=(CLAUDE.md PROGRESS.md docs/GDD.md docs/DESIGN_BRIEF.md play-dev.bat release)

index="$(git rev-parse --git-dir)/public-index"
rm -f "$index"
GIT_INDEX_FILE="$index" git read-tree HEAD
GIT_INDEX_FILE="$index" git rm -r -q --cached --ignore-unmatch "${PRIVATE[@]}"
tree=$(GIT_INDEX_FILE="$index" git write-tree)
rm -f "$index"

id=$(gh api user --jq .id)
login=$(gh api user --jq .login)
email="${id}+${login}@users.noreply.github.com"
parent=()
if git rev-parse -q --verify refs/heads/public >/dev/null; then parent=(-p refs/heads/public); fi
commit=$(GIT_AUTHOR_NAME=Mebb GIT_AUTHOR_EMAIL="$email" GIT_COMMITTER_NAME=Mebb GIT_COMMITTER_EMAIL="$email" \
    git commit-tree "$tree" "${parent[@]}" -m "$message")
git update-ref refs/heads/public "$commit"
echo "public: $commit ($(git ls-tree -r --name-only "$commit" | wc -l) files)"
git push public public:main
