<template>
  <header
    class="bg-white py-3 px-6 lg:py-4 sticky top-0 z-50 border-b-2 border-primary-100 shadow-sm flex items-center justify-between md:justify-center space-x-6">
    <HeaderLogo />
    <ProductSearchInput class="hidden md:flex" />
    <NavBarItems additionalClass="hidden md:flex space-x-6 justify-center items-center" />

    <div class="md:hidden cursor-pointer space-x-6 text-l flex justify-center items-center">
      <SearchItem @toggleSearchInput="toggleSearchInput" />
      <HamburgerIcon :handleOnClick="toggleAsideVisibility" />
      <ShoppingCartItem />
    </div>
    <ProductSearchDropdown :isOpen="isSearchInputOpen" :onClose="closeSearchInput"
      :onClickOutside="handleClickOutsideSearchInput" />
  </header>
  <HamburgerDropdown :isOpen="isAsideOpen" :onClose="closeAside" :onClickOutside="handleClickOutsideAside" />
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import HeaderLogo from './HeaderLogo.vue'
import ProductSearchInput from '../ProductSearchInput.vue';
import HamburgerIcon from './HamburgerIcon.vue'
import SearchItem from './SearchItem.vue';
import NavBarItems from './NavBarItems.vue';
import ShoppingCartItem from './ShoppingCartItem.vue';
import HamburgerDropdown from './HamburgerDropdown.vue';
import ProductSearchDropdown from '../products/ProductSearchDropdown.vue';

export default defineComponent({
  name: "NavBar",

  setup() {
    const isAsideOpen = ref<boolean>(false);
    const isSearchInputOpen = ref(false);

    function toggleAsideVisibility() {
      isAsideOpen.value = !isAsideOpen.value
    }

    function closeAside() {
      isAsideOpen.value = false
    }

    function handleClickOutsideAside(event: any) {
      if (event.target.className.includes("outside-aside-components")) {
        closeAside()
      }
    }

    function toggleSearchInput(isShowingSearchInput: boolean) {
      isSearchInputOpen.value = isShowingSearchInput;
    }

    function handleClickOutsideSearchInput(event: any) {
      if (event.target.className.includes("outside-search-components")) {
        closeSearchInput()
      }
    }

    function closeSearchInput() {
      isSearchInputOpen.value = false;
    }

    return {
      isAsideOpen,
      toggleAsideVisibility,
      closeAside,
      handleClickOutsideAside,
      isSearchInputOpen,
      toggleSearchInput,
      handleClickOutsideSearchInput,
      closeSearchInput
    }

  },
  components: {
    HeaderLogo, ProductSearchInput, SearchItem, HamburgerIcon, NavBarItems, ShoppingCartItem, HamburgerDropdown,
    ProductSearchDropdown
  },
})
</script>
