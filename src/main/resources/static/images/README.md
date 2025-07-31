# Image Storage Directory

This directory contains uploaded images for the e-commerce application.

## Directory Structure:
- `products/` - Product images uploaded by sellers

## File Naming Convention:
- Original: `laptop.jpg`
- Saved as: `laptop_20241231_143022.jpg` (timestamp added)
- Database stores: `images/products/laptop_20241231_143022.jpg`

## Features:
- Automatic directory creation
- Unique filename generation with timestamps
- File type validation (images only)
- Automatic cleanup when products are deleted

## Deployment:
- Images are included in the JAR/WAR file
- No manual file copying required
- Works on any system where the application is deployed 