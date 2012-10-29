package sage2D.images

import java.awt.Color
import java.awt.Composite
import java.awt.image.ColorModel
import java.awt.image.Raster
import java.awt.image.WritableRaster
import java.awt.RenderingHints
import java.awt.CompositeContext
import scala.collection.mutable.ListBuffer


/** 
 * Produces a combined java.awt.Composite by applying the effects of multiple CompositeContexts. 
 * Warning: It turns out that at the time of writing, custom Composites are very slow. You'd probably be 
 * better off using one of the ImageEffects methods and caching the result image or using this.
 * to render only a few sprites. 
 */
class MultiComposite extends java.awt.Composite with CompositeContext {
    val blends = new ListBuffer[PixelBlender]
    
    def addOpacityBlender(alpha : Double) : Unit = {
        blends += new OpacityBlender(alpha)
    }
    
    def createContext(srcCM : ColorModel, dstCM : ColorModel, hints : RenderingHints) : CompositeContext = {
        this
    }
    
    
    def compose(src : Raster, dstIn : Raster, dstOut : WritableRaster) : Unit = {
        // If there are no blends to apply, just output our original dst pixels.
        if(this.blends.isEmpty) {
            dstOut.setRect(src)
            return
        }
        
        var lastDst : Raster = dstIn
        
        // apply each of our PixelBlenders in order to create our final raster
        val minX = dstIn.getMinX
        val minY = dstIn.getMinY
        val maxX = minX + dstIn.getWidth
        val maxY = minY + dstIn.getHeight
        
        var srcPix = new Array[Int](4)
        var dstPix = new Array[Int](4)
        
        for(blend <- blends) {
            for(y <- minY until maxY) {
                for(x <- minX until maxX) {
                    srcPix = src.getPixel(x,y, srcPix)
                    dstPix = lastDst.getPixel(x,y, dstPix)
                    
                    // apply the current blend to this pixel.
                    var result = blend(srcPix, dstPix)
 
                    // set our resulting pixel
                    dstOut.setPixel(x,y, result)
                }
            }
            // this iteration's dstOut will be our dstIn for the next iteration.
            lastDst = dstOut
        }
    }
    
    def dispose : Unit = {}
}


trait PixelBlender {
    def apply(src : Array[Int], dst : Array[Int]) : Array[Int]
}

/** Applies opacity multiplicatively to pixel. */
class OpacityBlender(val alpha : Double) extends PixelBlender {
    def apply(src : Array[Int], dst : Array[Int]) : Array[Int] = {
        var result = new Array[Int](4)
        
        result(0) = (dst(0) + (src(0) - dst(0))*alpha).toInt
        result(1) = (dst(1) + (src(1) - dst(1))*alpha).toInt
        result(2) = (dst(2) + (src(2) - dst(2))*alpha).toInt
        result(3) = (dst(3) + (src(3) - dst(3))*alpha).toInt
        
        result
    }
}



