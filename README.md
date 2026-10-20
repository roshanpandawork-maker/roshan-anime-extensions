# Roshan Anime Extensions

A standalone project for tracking and developing Aniyomi-compatible anime sources, prioritising Hindi-dubbed and English-subbed/dubbed anime.

> **Status:** Initial setup. This repository is not yet an installable Aniyomi extension repository: it does not currently publish signed APKs or an `index.min.json`. I will not label a source as working until it has been built and tested.

## Current priorities

1. HiAnime — English subtitles/dubs where available.
2. Hindi-focused sources — Hindi-dubbed anime.
3. Other maintained sources — English sub/dub as available.

## Sources you can use now

These are independent community repositories, not bundled into this project. Add them separately in Aniyomi under **Settings → Browse → Extension repos**:

- **Yūzōnō anime extensions**  
  Repository index: https://raw.githubusercontent.com/yuzono/anime-repo/repo/index.min.json  
  Source code: https://github.com/yuzono/anime-extensions

- **MSR anime extensions** (their published catalogue lists Toonstream, AnimeDubHindi and MovieBox)  
  Repository index: https://raw.githubusercontent.com/msrofficial/anime-repo/repo/index.min.json  
  Source code: https://github.com/msrofficial/anime-extensions

Availability can change when streaming sites change domains, block requests, or alter their pages. Do not assume every source is currently functional.

## Project rules

- This repository stays separate from other personal projects.
- Work is committed to `main`; no unrelated repositories or files are modified.
- Source code and APKs must be traceable to their source and license.
- Do not publish unsigned, unverifiable, or falsely labelled APKs.
- Test search, details, episode listing, and playback before marking a source as working.

## Planned work

- [ ] Establish the extension build template and CI.
- [ ] Implement and test a HiAnime source.
- [ ] Evaluate maintained Hindi sources and implement only those that can be tested.
- [ ] Generate the repository index and signed APKs before publishing an install URL.

## Legal and safety note

This project provides source-integration software, not anime video files. Users are responsible for following applicable law and the terms of the sites they access. Extensions can stop working without notice.

## References

- https://github.com/aniyomiorg/aniyomi
- https://github.com/yuzono/anime-extensions
- https://github.com/msrofficial/anime-extensions
