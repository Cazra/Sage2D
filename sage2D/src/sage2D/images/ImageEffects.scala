package sage2D.images

import java.awt.Image
import java.awt.Toolkit
import java.awt.Color
import java.awt.image.FilteredImageSource
import java.awt.image.RGBImageFilter
import java.awt.image.PixelGrabber
import java.awt.image.ImageObserver


/** Provides static methods for producing various types of filtered images useful for a gaming context. */
object ImageEffects {
    
    /** Produces an image with one color set to be completely transparent. */
    def transparentColor(img : Image, transColor : Color) : Image = {
        val filter = new RGBImageFilter {
            val transHex = transColor.getRGB | 0xFF000000
            override def filterRGB(x : Int, y : Int, rgb : Int) : Int = {
                // if the current pixel's opaque color matches transColor, then make this pixel completely transparent.
                if((rgb | 0xFF000000) == transHex) {
                    rgb & 0x00FFFFFF
                }
                else
                    rgb
            }
        }
        
        val src = img.getSource
        val fis = new FilteredImageSource(src, filter)
        Toolkit.getDefaultToolkit.createImage(fis)
    }
    
    /** Makes all the pixels in the resulting image opaque. */
    def makeOpaque(img : Image) : Image = {
        val filter = new RGBImageFilter {
            override def filterRGB(x : Int, y : Int, rgb : Int) : Int = {
                    rgb | 0xFF000000
            }
        }
        
        val src = img.getSource
        val fis = new FilteredImageSource(src, filter)
        Toolkit.getDefaultToolkit.createImage(fis) 
    }
    
    /** Produces an image with a uniform opacity applied to all pixels that aren't completely transparent. */
    def setOpacity(img : Image, aalpha : Int) : Image = {
        // normalize alpha to be an integer in [0,255]
        val alpha = if(aalpha > 0xff) 0xff
                else if(aalpha < 0) 0
                else aalpha
        
        val filter = new RGBImageFilter {
            override def filterRGB(x : Int, y : Int, rgb : Int) : Int = {
                // if the pixel's alpha isn't 0, set its alpha to alpha.
                if((rgb & 0xFF000000) != 0) {
                    rgb & ((alpha << 24) | 0x00FFFFFF)
                }
                else rgb
            }
        }
        
        val src = img.getSource
        val fis = new FilteredImageSource(src, filter)
        Toolkit.getDefaultToolkit.createImage(fis)
    }
    
    /** Produces an image with a uniform opacity applied to all pixels that aren't completely transparent. */
    def setOpacity(img : Image, aalpha : Double) : Image = {
        setOpacity(img, (aalpha*255).toInt)
    }
    
    
    /** Produces an image whose pixels' alpha values are scaled from the original image. */
    def multOpacity(img : Image, aalpha : Double) : Image = {
        // normalize alpha to be a floating point number in [0.0, 1.0]
        val alpha = if(aalpha > 1.0) 1.0
                else if(aalpha < 0.0) 0.0
                else aalpha
        
        val filter = new RGBImageFilter {
            override def filterRGB(x : Int, y : Int, rgb : Int) : Int = {
                val newalpha = ((rgb & 0xFF000000) * alpha).toInt & 0xFF000000
                (rgb & 0x00FFFFFF) + newalpha
            }
        }
        
        val src = img.getSource
        val fis = new FilteredImageSource(src, filter)
        Toolkit.getDefaultToolkit.createImage(fis)
    }
    
    /** Produces an image whose pixels' alpha values are scaled from the original image. */
    def multOpacity(img : Image, aalpha : Int) : Image = {
        multOpacity(img, aalpha/255.0)
    }
    
    /** Produces an image with alpha values obtained from an alpha map image. */
    def alphaMap(img : Image, alphaMap : Image, width : Int, height : Int) : Image = {
        // attempt to get the alpha map's pixels.
        val aPix = new Array[Int](width*height)
        val pg = new PixelGrabber(alphaMap, 0, 0, width, height, aPix, 0, width)
        try {
            pg.grabPixels
        }
        catch {
            case _ => return img
        }
        if ((pg.getStatus & ImageObserver.ABORT) != 0) {
            return img
        }
        
        // apply values from the alpha map to their corresponding pixels.
        val filter = new RGBImageFilter {
            override def filterRGB(x : Int, y : Int, rgb : Int) : Int = {
                // if the pixel isn't already completely transparent, apply the alpha map to it.
                if((rgb & 0xFF000000) == 0) rgb
                else {
                    val alpha = (aPix(y*width + x) & (0x00FF0000 >> 16))
                    (rgb & 0x00FFFFFF) + (alpha << 24)
                }
            }
        }
        
        val src = img.getSource
        val fis = new FilteredImageSource(src, filter)
        Toolkit.getDefaultToolkit.createImage(fis)
    }
    
    /** Produces an an image with inverted RGB values. */
    def invert(img : Image) : Image = {
        val filter = new RGBImageFilter {
            override def filterRGB(x : Int, y : Int, rgb : Int) : Int = {
                (rgb & 0xFF000000) + ((~rgb) & 0x00FFFFFF)
            }
        }
        
        val src = img.getSource
        val fis = new FilteredImageSource(src, filter)
        Toolkit.getDefaultToolkit.createImage(fis)
    }
    
    
}

