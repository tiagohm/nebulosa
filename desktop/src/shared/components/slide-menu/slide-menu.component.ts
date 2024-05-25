import { Component, ElementRef, EventEmitter, Input, OnInit, Output, TemplateRef } from '@angular/core'
import { ExtendedMenuItem, ExtendedMenuItemCommandEvent } from '../menu-item/menu-item.component'

export interface SlideMenuItem extends ExtendedMenuItem {
    command?: (event: SlideMenuItemCommandEvent) => void
}

export interface SlideMenuItemCommandEvent extends ExtendedMenuItemCommandEvent {
    item?: SlideMenuItem
    parent?: SlideMenuItem
    level?: number
}

export type SlideMenu = SlideMenuItem[]

@Component({
    selector: 'neb-slide-menu',
    templateUrl: './slide-menu.component.html',
    styleUrls: ['./slide-menu.component.scss'],
})
export class SlideMenuComponent implements OnInit {

    @Input({ required: true })
    readonly model!: SlideMenu

    @Input()
    readonly appendTo: HTMLElement | ElementRef | TemplateRef<any> | string | null | undefined | any

    @Output()
    readonly onNext = new EventEmitter<SlideMenuItemCommandEvent>()

    @Output()
    readonly onBack = new EventEmitter<any>()

    menu!: SlideMenu
    private readonly navigation: SlideMenu[] = []

    ngOnInit() {
        this.processMenu(this.model, 0)
        this.menu = this.model
    }

    back(event: MouseEvent) {
        if (this.navigation.length) {
            this.menu = this.navigation.splice(this.navigation.length - 1, 1)[0]
            this.onBack.emit(undefined)
        }
    }

    private processMenu(menu: SlideMenu, level: number, parent?: SlideMenuItem) {
        for (const item of menu) {
            const command = item.command

            if (item.menu?.length) {
                item.command = (event: SlideMenuItemCommandEvent) => {
                    this.menu = item.menu!
                    this.navigation.push(menu)
                    event.parent = parent
                    event.level = level
                    command?.(event)
                    this.onNext.emit(event)
                }

                this.processMenu(item.menu, level + 1, item)
            } else {
                item.command = (event: SlideMenuItemCommandEvent) => {
                    event.parent = parent
                    event.level = level
                    command?.(event)
                    this.onNext.emit(event)
                }
            }
        }
    }
}
