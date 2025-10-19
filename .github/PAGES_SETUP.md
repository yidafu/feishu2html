# GitHub Pages Setup Guide

This guide explains how to enable GitHub Pages for this repository to host the API documentation.

## Prerequisites

- Repository must be public (or you have GitHub Pro for private repositories)
- You have admin access to the repository

## Steps to Enable GitHub Pages

1. **Navigate to Repository Settings**
   - Go to your repository on GitHub
   - Click on **Settings** tab

2. **Configure Pages**
   - In the left sidebar, click on **Pages**
   - Under **Source**, select **GitHub Actions**
   - Click **Save**

3. **Trigger Documentation Build**
   - Push any changes to the `main` branch, or
   - Go to **Actions** tab → **Deploy API Documentation** → **Run workflow**

4. **Access Your Documentation**
   - Once the workflow completes, your documentation will be available at:
   - `https://<username>.github.io/<repository-name>/`
   - Example: `https://yourusername.github.io/feishu2html/`

## Workflow Details

The GitHub Action workflow (`.github/workflows/deploy-docs.yml`) automatically:

- Triggers on every push to `main` branch
- Sets up JDK 17
- Runs `./gradlew dokkaHtml` to generate documentation
- Deploys the generated HTML to GitHub Pages

## Updating Documentation URLs

After setting up GitHub Pages, update the following in `README.md`:

Replace `https://yourusername.github.io/feishu2html/` with your actual GitHub Pages URL.

## Troubleshooting

### Documentation not showing up

1. Check the **Actions** tab for any failed workflow runs
2. Ensure GitHub Pages is enabled in Settings → Pages
3. Verify the source is set to **GitHub Actions**

### Permission errors

The workflow requires the following permissions (already configured):
- `contents: read`
- `pages: write`
- `id-token: write`

These should be automatically granted when using GitHub Actions for Pages.

## Manual Deployment

If you prefer to deploy manually:

```bash
# Generate documentation locally
./gradlew dokkaHtml

# The documentation will be in build/dokka/html/
# You can deploy this directory to any static hosting service
```

