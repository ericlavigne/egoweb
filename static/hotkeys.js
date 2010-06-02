
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
			if(i < 9) {
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
			if(event.keyCode > 48 && event.keyCode < 58 
				&& jQuery('.textfocus').length === 0) 
			{
				jQuery("input.hotkey").each(function(i) {
					if(i == event.keyCode - 49) {
						jQuery(this).click();
						jQuery(this).triggerHandler('click');
					}
				});
			}
		});
		applyPageChangesForHotkeys();
		window.setInterval('applyPageChangesForHotkeys()', 1000);
	}
});

