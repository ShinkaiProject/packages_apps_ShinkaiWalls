package com.shinkai.wallpapers

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.MeasureSpec
import android.view.animation.PathInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.color.MaterialColors
import kotlin.math.max

/**
 * Compact floating navigation bar used on the top level destinations of the app.
 *
 * It is intentionally not a full width [android.widget.BottomNavigationView]: the container
 * hugs its content, keeps horizontal margins from the screen edges, floats above the bottom
 * edge and follows the Shinkai Walls theme, so it reads as a native extension of the app
 * instead of a bolted on navigation bar.
 *
 * The bar never owns navigation state. Clicks only report the requested [TopLevelDestination]
 * and the active pill is rendered exclusively by [showDestination], which the owning activity
 * calls from `onResume`. That keeps the pill and the real destination in sync and stops a
 * previous selection from getting stuck after a click that did not end up navigating.
 */
class FloatingNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private class Holder(
        val root: View,
        val icon: ImageView,
        val label: TextView,
        val pill: GradientDrawable
    )

    private val holders = LinkedHashMap<Int, Holder>()

    /**
     * Last destination rendered by the bar. It is only written by [showDestination], never by a
     * click, and exists just to skip redundant animations when an activity resumes again.
     */
    private var renderedDestination: TopLevelDestination? = null
    private var onItemSelected: ((TopLevelDestination) -> Unit)? = null
    private val baseMarginBottom: Int =
        (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0

    /** Material emphasised decelerate curve, used by every state change of the bar. */
    private val motion: TimeInterpolator = PathInterpolator(0.2f, 0f, 0f, 1f)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        // The activity roots carry android:fitsSystemWindows, so they already lift the whole
        // content above the system bars and consume the insets before they reach this view.
        // That clearance must therefore stay outside of the bar: padding it here would stretch
        // the pill and leave a dead band under the items on top of the clearance the root added.
        // A margin keeps the pill at its designed height and shape either way.
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val bottom = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            ).bottom
            (view.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
                val target = baseMarginBottom + bottom
                if (params.bottomMargin != target) {
                    params.bottomMargin = target
                    view.layoutParams = params
                }
            }
            insets
        }
    }

    fun setOnItemSelectedListener(listener: (TopLevelDestination) -> Unit) {
        onItemSelected = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        applyFullyRoundedShape(h)
    }

    /**
     * Turns the floating container into a real capsule by giving every corner radius half of the
     * measured height, which is what makes both ends perfect semicircles. The height is not fixed
     * at build time because it follows the item height, the font scale and the bottom inset, so
     * the token in `bg_floating_nav` is only the declared default.
     *
     * Only the shape is touched: the colour, the stroke and therefore the current style of the
     * toolbar are kept, and this runs before the first draw so the pill never appears rounded
     * only halfway.
     */
    private fun applyFullyRoundedShape(height: Int) {
        val shape = background as? GradientDrawable ?: return
        shape.mutate()
        val radii = FloatArray(8) { height / 2f }
        if (shape.cornerRadii?.contentEquals(radii) == true) return
        shape.cornerRadii = radii
    }

    /** Fills the bar with [destinations]; no item is active until one is rendered. */
    fun setItems(destinations: List<TopLevelDestination>) {
        removeAllViews()
        holders.clear()

        val inflater = LayoutInflater.from(context)
        for (destination in destinations) {
            val root = inflater.inflate(R.layout.view_floating_nav_item, this, false)
            val icon = root.findViewById<ImageView>(R.id.nav_item_icon)
            val label = root.findViewById<TextView>(R.id.nav_item_label)
            val pill = createPill()

            icon.setImageResource(destination.iconRes)
            label.setText(destination.titleRes)
            root.contentDescription = context.getString(destination.titleRes)
            root.background = ripple(pill)
            root.setOnClickListener { onItemSelected?.invoke(destination) }

            holders[destination.navItemId] = Holder(root, icon, label, pill)
            addView(root)
        }

        equaliseSlots()

        val rendered = renderedDestination
        renderedDestination = null
        if (rendered != null) showDestination(rendered)
    }

    /**
     * Gives every destination the same fixed slot width.
     *
     * Each item is inflated as `wrap_content`, so on its own its width would follow the length of
     * its own label: the Beranda slot and the Dinding slot would differ, the outer bar would be
     * sized by the text of the destinations, and the indicator would travel a different distance
     * in each direction. Sizing every slot to the widest label keeps both slots identical and the
     * outer bounds of the bar identical for every destination, without hardcoding a width: it
     * still follows the locale, the font scale and the screen.
     */
    private fun equaliseSlots() {
        if (holders.size < 2) return
        val unmeasured = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        var slot = 0
        for (holder in holders.values) {
            holder.root.measure(unmeasured, unmeasured)
            slot = max(slot, holder.root.measuredWidth)
        }
        for (holder in holders.values) {
            holder.root.layoutParams = holder.root.layoutParams.apply { width = slot }
        }
    }

    /**
     * Renders [destination] as the active item. This is the only writer of the selection, so
     * calling it with the destination that `onResume` reports keeps the pill truthful across
     * creation, back navigation, `CLEAR_TOP` reuse, background returns and recreation.
     */
    fun showDestination(destination: TopLevelDestination) {
        if (renderedDestination == destination) return

        val next = holders[destination.navItemId] ?: return
        val previous = renderedDestination?.let { holders[it.navItemId] }
        val animate = previous != null

        previous?.let { holder ->
            holder.root.isSelected = false
            tint(holder, selected = false)
            fadePill(holder, activeColor(), Color.TRANSPARENT, animate)
        }

        renderedDestination = destination
        next.root.isSelected = true
        tint(next, selected = true)
        fadePill(next, Color.TRANSPARENT, activeColor(), animate)
    }

    private fun fadePill(holder: Holder, from: Int, to: Int, animate: Boolean) {
        if (!animate) {
            holder.pill.setColor(to)
            return
        }
        if (from == to) return
        ValueAnimator.ofArgb(from, to).apply {
            duration = PILL_DURATION_MS
            interpolator = motion
            addUpdateListener { holder.pill.setColor(it.animatedValue as Int) }
            start()
        }
    }

    private fun tint(holder: Holder, selected: Boolean) {
        val attribute = if (selected) {
            com.google.android.material.R.attr.colorOnSecondaryContainer
        } else {
            com.google.android.material.R.attr.colorOnSurfaceVariant
        }
        val color = ColorStateList.valueOf(MaterialColors.getColor(holder.icon, attribute))
        holder.icon.imageTintList = color
        holder.label.setTextColor(color.defaultColor)
    }

    /**
     * Capsule background of a navigation item. The radius is clamped by the framework to half of
     * each side, so the pill always hugs its item, whatever the label width is. It is independent
     * from the corner radius of the floating toolbar container.
     */
    private fun createPill(): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = PILL_CORNER_RADIUS
        setColor(Color.TRANSPARENT)
    }

    private fun ripple(content: GradientDrawable) = RippleDrawable(
        ColorStateList.valueOf(MaterialColors.getColor(this, android.R.attr.colorControlHighlight)),
        content,
        null
    )

    private fun activeColor(): Int = MaterialColors.getColor(
        this,
        com.google.android.material.R.attr.colorSecondaryContainer
    )
}

private const val PILL_DURATION_MS = 180L
private const val PILL_CORNER_RADIUS = Float.MAX_VALUE