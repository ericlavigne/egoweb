
// Keys 1-9 can be used to select radio buttons or checkboxes with class="hotkey".

// This feature is turned off while editing text in a textfield or textarea.

// jQuery version 1.4.2 should be loaded before this script.

jQuery.noConflict();

function applyPageChangesForHotkeys() {
	// Add labels so we know which numbers select which radio buttons or checkboxes.
	jQuery('span.hotkey').text(function(i,t) {
		// if doesn't have hotkeymodified class
		// add hotkeymodified class and do the following
		if(! jQuery(this).hasClass('hotkeymodified')) {
			jQuery(this).addClass('hotkeymodified');
			var hk = jQuery(this).attr('hotkey');
			var cnt = jQuery('span.hotkey').filter(function(index) {
				return jQuery(this).attr('hotkey') == hk;
			}).length;
			if(hk) {
				if(cnt == 1) {
					return '(' + hk + ') ' + t;
				} else {
					return t;
				}
			} else if(i < 9) {
				return '(' + (i+1) + ') ' + t;
			} else {
				return t;
			}
		}
	});
	// Mark text input fields with the textfocus class when they are in focus.
	jQuery('input[type=text],textarea').each(function() {
		// if doesn't have hotkeymodified class
		// add hotkeymodified class, and do the following
		if(! jQuery(this).hasClass('hotkeymodified')) {
			jQuery(this)
			.addClass('hotkeymodified')
			.blur(function() {
				jQuery(this).removeClass("textfocus");
			})
			.focus(function() {
				jQuery(this).addClass("textfocus");
			});
		}
	});
}


jQuery(document).ready(function(){
	// Only do hotkeys if we have enough keys for all.
	if(jQuery('span.hotkey').length < 10) {
		// Handle keydown event for keys 1-9.
		jQuery(document).keydown(function(event) {
			if(jQuery('.textfocus').length === 0) 
			{
				jQuery("input.hotkey").each(function(i) {
					var hk = jQuery(this).attr('hotkey');
					if(hk) {
						if(hk == charForKeycode(event.which)) {
							var cnt = jQuery('span.hotkey').filter(function(index) {
								return jQuery(this).attr('hotkey') == hk;
							}).length;
							if(cnt == 1) {
								jQuery(this).focus();
								if(jQuery(this).attr("type") === "checkbox") {
									jQuery(this).click();
								} else {
									jQuery(this).attr("checked","checked");
								}
								jQuery(this).triggerHandler('click');
							}
						}
					} else if(i == event.keyCode - 49) {
						jQuery(this).focus();
						if(jQuery(this).attr("type") === "checkbox") {
							jQuery(this).click();
						} else {
							jQuery(this).attr("checked","checked");
						}
						jQuery(this).triggerHandler('click');
					}
				});
			}
		});
		applyPageChangesForHotkeys();
		window.setInterval('applyPageChangesForHotkeys()', 1000);
	}
});

function charForKeycode(keycode) {
	return {
		48: "0", 49: "1", 50: "2", 51: "3", 52: "4",
		53: "5", 54: "6", 55: "7", 56: "8", 57: "9",
		
		65: "A", 66: "B", 67: "C", 68: "D", 69: "E",
		70: "F", 71: "G", 72: "H", 73: "I", 74: "J",
		75: "K", 76: "L", 77: "M", 78: "N", 79: "O",
		80: "P", 81: "Q", 82: "R", 83: "S", 84: "T",
		85: "U", 86: "V", 87: "W", 88: "X", 89: "Y",
		90: "Z",
		
		112: "F1", 113: "F2", 114: "F3", 115: "F4",
		116: "F5", 117: "F6", 118: "F7", 119: "F8",
		120: "F9", 121: "F10", 122: "F11", 123: "F12"
	}[keycode];
}
