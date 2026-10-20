# Roshan Anime Extensions

A standalone Aniyomi-compatible anime extension project prioritising Hindi-dubbed and English-subbed/dubbed anime.

> **Status: prototype, not production-ready.** A first HiAnime module and a GitHub Actions build workflow have been added. The module has not yet been built or tested in a live Aniyomi installation, and playback extraction is not implemented. This repository does not yet publish a usable repository index or verified release APK. The original HiAnime service's status is disputed; treat `hianime.to` and lookalike domains as unverified until independently confirmed.

## Current implementation

- **HiAnime (English):** initial Kotlin module for popular listings, search, details, and episode-list parsing.
- **Playback:** not implemented. HiAnime pages commonly use third-party protected embeds; the module deliberately reports this limitation rather than passing an iframe URL off as a playable video.
- **Hindi sources:** under evaluation. They are not claimed as implemented or tested here.

The Build HiAnime extension workflow attempts to build the module against the archived Aniyomi extension build environment and uploads a debug APK artifact if compilation succeeds. A successful build alone does not prove the site parser or playback works.

## Sources you can use separately now

These community repositories are independent of this project. Add them separately in Aniyomi under **Settings → Browse → Extension repos**:

- **Yūzōnō anime extensions**
  Repository index: https://raw.githubusercontent.com/yuzuno/anime-repo/repo/index.min.json
  Source code: https://github.com/yuzono/anime-extensions

- **MSR anime extensions** (their published catalogue lists Toonstream, AnimeDubHindi and MovieBox)
  Repository index: https://raw.githubusercontent.com/msrofficial/anime-repo/repo/index.min.json
  Source code: https://github.com/msrofficial/anime-extensions

Availability changes when streaming sites change domains, block requests, or alter their pages. The original HiAnime community announced a shutdown in 2026, while third-party sites make conflicting claims about replacement domains; do not trust random lookalike domains or enter credentials on them. These links are independent third-party repositories, not APKs built by Roshan Anime Extensions.

## Project rules

- This repository stays separate from other personal projects.
- Work is committed to main; no unrelated repositories or files are modified.
- Source code and APKs must be traceable to their source and license.
- Never commit signing keystores or signing passwords.
- Test search, details, episode listing, and playback before marking a source as working.
- Do not publish a repository index or advertise a production-ready release until a reproducible build and real playback tests pass.

## Next milestones

- [x] Add the initial HiAnime module and a build workflow.
- [ ] Confirm the GitHub Actions build succeeds; fix compiler/build errors.
- [ ] Test search, details, and episode listing against the current site.
- [ ] Integrate and test a maintained video extractor.
- [ ] Evaluate Hindi sources and implement only sources that can be tested.
- [ ] Set up signed releases and generate a repository index only after the above passes.

## Legal and safety note

This project provides source-integration software, not anime video files. Users are responsible for following applicable law and the terms of the sites they access. Community extensions can stop working without notice.

## References

- https://github.com/aniyomiorg/aniyomi
- https://github.com/yuzono/anime-extensions
- https://github.com/msrofficial/anime-extensions
