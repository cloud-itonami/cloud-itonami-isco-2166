# cloud-itonami-isco-2166

Open Occupation Blueprint for **ISCO-08 2166**: Graphic and Multimedia Designers.

This repository designs a forkable OSS business for an independent graphic/multimedia designer: a print-proofing robot performs physical proof printing and print-quality scanning under a governor-gated actor, so the studio keeps its own project and licensing records instead of renting a closed design-management SaaS.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a print-proofing robot performs physical proof printing, material handling and print-quality scanning under an actor that proposes
actions and an independent **Graphic Design Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
using copyrighted assets without license verification, or client-brand-guideline overrides) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
client brief + brand guidelines + delivery format
        |
        v
Design Advisor -> Graphic Design Governor -> design/deliver, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `2166`). Required capabilities:

- :robotics
- :identity
- :forms
- :dmn
- :bpmn
- :audit-ledger

## Reference actor (`:maturity :implemented`)

Full itonami Actor pattern (like
[`cloud-itonami-isco-6130`](https://github.com/cloud-itonami/cloud-itonami-isco-6130) /
[`-2652`](https://github.com/cloud-itonami/cloud-itonami-isco-2652)): a real
[`kotoba-lang/langgraph`](https://github.com/kotoba-lang/langgraph)
`StateGraph` with Advisor and Governor as distinct nodes and human-in-the-loop
interrupt/resume.

- HARD → `:hold`: unregistered project, non-`:propose` effect.
- ESCALATE → `:request-approval` (human-signed): any asset outside the
  project's licensed set (license verification), client brand-guideline
  overrides, low confidence.

```bash
kbb -M:test
```

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
