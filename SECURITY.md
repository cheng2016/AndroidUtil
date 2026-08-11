# Security policy

## Supported versions

Only the latest stable release receives security fixes.

## Reporting a vulnerability

Do not open a public issue. Email `mitnick.cheng@outlook.com` with:

- the affected module and version;
- reproduction steps or a proof of concept;
- expected impact and any suggested mitigation.

We aim to acknowledge reports within 5 business days. Please allow time for a coordinated fix before public disclosure.

AndroidUtil intentionally excludes device-identifier collection, hidden-API hooks, payment SDK wrappers, shared-storage secrets, and release HTTP body logging. Reintroducing any of these requires a security review.
