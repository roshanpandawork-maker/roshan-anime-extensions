# Source development and verification

## Current state

This repository is in setup/research stage. No source in `sources.json` has been implemented or tested by this project, and there is no installable extension APK or repository index yet.

## Priority order

1. **HiAnime** — English subtitles and dubs where the site provides them.
2. **Hindi sources** — evaluate AnimeDubHindi and Toonstream first.
3. **Additional maintained sources** — add only after checking current activity, source code, licence, and actual playback.

## Definition of done for a source

A source must not be marked working until all of these have been tested in a compatible Aniyomi/Anikku build:

- Search returns sensible results.
- Anime details and cover images load.
- Episode list loads and order is correct.
- Episode selection resolves a playable stream.
- Subtitle/audio options are represented correctly when available.
- Pagination and common errors are handled.
- No credentials, private keys, or signing keystores are committed.
- The source module builds successfully in CI.

## Development approach

Use the maintained Yūzōnō extension project as the first reference for the current extension API and module conventions. The original Aniyomi extension repository is archived, so its old examples may not match current tooling.

- Current reference source: https://github.com/yuzono/anime-extensions
- Current reference install index: https://raw.githubusercontent.com/yuzono/anime-repo/repo/index.min.json
- Hindi source reference: https://github.com/msrofficial/anime-extensions
- Hindi source install index: https://raw.githubusercontent.com/msrofficial/anime-repo/repo/index.min.json
- Aniyomi app and extension information: https://aniyomi.org/

## Publishing gate

Do not publish an `index.min.json` or give users an install URL until the project has:

1. A reproducible Gradle build.
2. At least one implemented and tested extension.
3. A stable signing key stored only in GitHub Actions secrets.
4. An automated build/publish workflow.
5. A generated index whose APK URLs and checksums are verified.

## Safety and reliability

Community source extensions can stop working when sites change domains, APIs, HTML, video providers, or anti-bot protections. A successful build does not prove that playback works. Report tested dates and known limitations rather than promising permanent availability.
