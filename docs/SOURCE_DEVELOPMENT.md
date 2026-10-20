# Source development and verification

## Current state

A first HiAnime Kotlin module has been added under src/en/hianime, along with a GitHub Actions workflow that attempts to compile it against the archived Aniyomi extension build environment. It is still a prototype: build status, live parsing, and playback have not been verified. The video-provider extractor is explicitly unfinished.

## Priority order

1. **HiAnime** — English subtitles and dubs where the site provides them.
2. **Hindi sources** — evaluate AnimeDubHindi and Toonstream first.
3. **Additional maintained sources** — add only after checking activity, source code, licence, and actual playback.

## Definition of done for a source

A source must not be marked working until all of these have been tested in a compatible Aniyomi/Anikku build:

- Search returns sensible results.
- Anime details and cover images load.
- Episode list loads and order is correct.
- Episode selection resolves a playable stream.
- Subtitle/audio options are represented correctly when available.
- Pagination and common errors are handled.
- The source module builds successfully in CI.
- No credentials, private keys, or signing keystores are committed.

## Build workflow

The Build HiAnime extension workflow checks out the archived aniyomiorg/aniyomi-extensions project, copies the module into that build environment, and attempts to build a debug APK. If successful, the APK is uploaded as a GitHub Actions artifact. This is a development artifact, not a published repository release. Check the Actions tab for the actual run result.

The old upstream build environment is archived. If its dependencies or build tooling no longer work, migrate the module to the current maintained extension API rather than silently publishing an unverified APK.

## References

- Current extension development guide: https://github.com/yuzono/anime-extensions/blob/master/CONTRIBUTING.md
- Current reference source: https://github.com/yuzono/anime-extensions
- Current reference install index: https://raw.githubusercontent.com/yuzono/anime-repo/repo/index.min.json
- Hindi source reference: https://github.com/msrofficial/anime-extensions
- Archived Aniyomi extension build environment: https://github.com/aniyomiorg/aniyomi-extensions

## Publishing gate

Do not publish an index.min.json or give users an install URL until the project has:

1. A reproducible build against a maintained or deliberately pinned build environment.
2. At least one implemented and tested extension.
3. A stable signing key stored only in GitHub Actions secrets.
4. An automated build/publish workflow.
5. A generated index whose APK URLs and checksums are verified.

## Reliability and safety

A successful compile does not prove playback works. Community sources can break when sites change domains, APIs, HTML, video providers, or anti-bot protections. Record the test date and known limitations.
