package com.example.gameboy.ui

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.gameboy.R

class OptionsActivity : BaseActivity() {

    // 0 = MUSIC, 1 = SOUND, 2 = STYLE
    private var selectedOption = 0

    private var tvMusic:  TextView? = null
    private var tvSound:  TextView? = null
    private var tvStyle:  TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        tvMusic = findViewById(R.id.tvMusicVol)
        tvSound = findViewById(R.id.tvSoundVol)
        tvStyle = findViewById(R.id.tvStyle)

        val dPadBase = findViewById<View>(R.id.dPadBase)

        updateTexts()

        // Вгору / Вниз — вибір пункту
        findViewById<View>(R.id.btnUp)?.addDpadRockingEffect(dPadBase, 15f, 0f) {
            soundManager.playClick()
            selectedOption = (selectedOption - 1 + 3) % 3
            updateTexts()
        }
        findViewById<View>(R.id.btnDown)?.addDpadRockingEffect(dPadBase, -15f, 0f) {
            soundManager.playClick()
            selectedOption = (selectedOption + 1) % 3
            updateTexts()
        }

        // Вліво
        findViewById<View>(R.id.btnLeft)?.addDpadRockingEffect(dPadBase, 0f, -15f) {
            when (selectedOption) {
                0 -> soundManager.setMusicVolume(
                    (soundManager.getMusicVolume() - 10).coerceAtLeast(0))
                1 -> {
                    soundManager.setSfxVolume(
                        (soundManager.getSfxVolume() - 10).coerceAtLeast(0))
                    soundManager.playEat()
                }
                2 -> {
                    val styles = styleProvider.styles
                    val idx = styles.indexOf(styleProvider.getStyle(this))
                    val prev = styles[(idx - 1 + styles.size) % styles.size]
                    styleProvider.setStyle(this, prev)
                    styleProvider.applyStyle(this)
                }
            }
            soundManager.playClick()
            updateTexts()
        }

        // Вправо
        findViewById<View>(R.id.btnRight)?.addDpadRockingEffect(dPadBase, 0f, 15f) {
            when (selectedOption) {
                0 -> soundManager.setMusicVolume(
                    (soundManager.getMusicVolume() + 10).coerceAtMost(100))
                1 -> {
                    soundManager.setSfxVolume(
                        (soundManager.getSfxVolume() + 10).coerceAtMost(100))
                    soundManager.playEat()
                }
                2 -> {
                    val styles = styleProvider.styles
                    val idx = styles.indexOf(styleProvider.getStyle(this))
                    val next = styles[(idx + 1) % styles.size]
                    styleProvider.setStyle(this, next)
                    styleProvider.applyStyle(this)
                }
            }
            soundManager.playClick()
            updateTexts()
        }

        val back = { soundManager.playClick(); finish(); overridePendingTransition(0, 0) }
        findViewById<View>(R.id.btnActionB)?.addBounceEffect(back)
        findViewById<View>(R.id.btnActionA)?.addBounceEffect(back)
        findViewById<View>(R.id.btnSelect)?.addBounceEffect(back)
    }

    private fun updateTexts() {
        val musicVol = soundManager.getMusicVolume()
        val sfxVol   = soundManager.getSfxVolume()
        val styleName = styleProvider.getStyleDisplayName(this)

        fun row(selected: Boolean, label: String) = if (selected) "> $label" else "  $label"

        tvMusic?.text = row(selectedOption == 0, "MUSIC: $musicVol%")
        tvMusic?.setTypeface(null, if (selectedOption == 0) Typeface.BOLD else Typeface.NORMAL)

        tvSound?.text = row(selectedOption == 1, "SOUND: $sfxVol%")
        tvSound?.setTypeface(null, if (selectedOption == 1) Typeface.BOLD else Typeface.NORMAL)

        tvStyle?.text = row(selectedOption == 2, "STYLE: $styleName")
        tvStyle?.setTypeface(null, if (selectedOption == 2) Typeface.BOLD else Typeface.NORMAL)
    }
}
